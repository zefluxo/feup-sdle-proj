package sdle.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import lombok.Getter;
import lombok.SneakyThrows;
import sdle.client.utils.HashUtils;
import sdle.crdt.implementations.ORMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static sdle.client.RestAdapter.SERVER_HOST;

@Getter
public class LocalStorage {
    private final String dataDir = System.getProperty("user.dir") + "/data";
    private final Map<String, ORMap> localShoppLists = new HashMap<>();
    private final RestAdapter cloudRestAdapter = new RestAdapter();
    ObjectMapper mapper;

    public LocalStorage() {
        mapper = new ObjectMapper();
        synchronise();
    }

    @SneakyThrows
    public void readAllFromDisk() {
        try (Stream<Path> pathsStream = Files.walk(Paths.get(dataDir), 1)
                .filter(Files::isRegularFile).onClose(() -> System.out.println("The Stream is closed"))) {
            for (Path path : pathsStream.toList()) {
                localShoppLists.put(path.getFileName().toString(), new ObjectMapper().readValue(new String(Files.readAllBytes(path)), ORMap.class));
            }
        }
    }

    public void synchronise() {
        System.out.println("Synchronizing lists from cloud");
        readAllFromDisk();
        localShoppLists.forEach((k, v) -> {
            v.join(getShoppList(k));
            sendShopListToCloud(k, v);
            System.out.printf("%s synchronized: %s%n", k, v);
        });
    }

    public void synchroniseShoppList(ORMap shoppList, String hashId) {
        if (localShoppLists.containsKey(hashId)) {
            localShoppLists.get(hashId).join(shoppList);
        } else {
            localShoppLists.put(hashId, shoppList);
        }
        writeOnDisk(hashId);
    }

    @SneakyThrows
    public void writeOnDisk(String hashId) {
        new ObjectMapper().writer().writeValue(Paths.get(dataDir, hashId).toFile(), localShoppLists.get(hashId));
    }

    public String newShoppList() {
        Optional<HttpResponse<Buffer>> response = cloudRestAdapter.sendSync(SERVER_HOST, HttpMethod.PUT, "/api/shopp/list");
        String hashId;
        if (response.isPresent() && response.get().statusCode() == 200) {
            hashId = response.get().bodyAsString();
        } else {
            hashId = HashUtils.getRandomHash();
        }
        synchroniseShoppList(new ORMap(), hashId);
        return hashId;
    }

    @SneakyThrows
    public ORMap getShoppList(String hashId) {
        localShoppLists.putIfAbsent(hashId, new ORMap());

        Optional<HttpResponse<Buffer>> response = cloudRestAdapter.
                sendSync(SERVER_HOST, HttpMethod.GET, String.format("/api/shopp/list/%s", hashId));
        ORMap shoppList = localShoppLists.get(hashId);
        if (response.isPresent() && response.get().statusCode() == 200) {
            shoppList.join(mapper.readValue(response.get().bodyAsString(), ORMap.class));
        }
        return shoppList;
    }

    private void sendShopListToCloud(String hashId, ORMap shoppList) {
        cloudRestAdapter.sendSync(SERVER_HOST, HttpMethod.POST, String.format("/api/shopp/list/%s", hashId), shoppList);
    }

    public void incOrDecItem(String hashId, String name, String quantity, boolean isInc) {
        localShoppLists.putIfAbsent(hashId, new ORMap());
        if (isInc) {
            localShoppLists.get(hashId).inc(name, Integer.valueOf(quantity));
        } else {
            localShoppLists.get(hashId).dec(name, Integer.valueOf(quantity));
        }

        cloudRestAdapter.sendSync(
                SERVER_HOST, HttpMethod.POST,
                String.format("/api/shopp/list/%s/%s/%s/%s", hashId, (isInc ? "inc" : "dec"), name, quantity));
        writeOnDisk(hashId);
        System.out.print(localShoppLists.get(hashId).getMap());
    }

    public void incItem(String hashId, String name, String quantity) {
        incOrDecItem(hashId, name, quantity, true);
    }

    public void decItem(String hashId, String name, String quantity) {
        incOrDecItem(hashId, name, quantity, false);
    }

}
