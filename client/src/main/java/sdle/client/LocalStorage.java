package sdle.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
        readAllFromDisk();
    }

    @SneakyThrows
    public void readAllFromDisk() {
        try (Stream<Path> pathsStream = Files.walk(Paths.get(dataDir), 1)
                .filter(Files::isRegularFile)) {
            for (Path path : pathsStream.toList()) {
                localShoppLists.put(path.getFileName().toString(), new ObjectMapper().readValue(new String(Files.readAllBytes(path)), ORMap.class));
            }
        }
    }

//    public void synchronise() {
//        System.out.println("Synchronizing lists from cloud");
//        readAllFromDisk();
//        localShoppLists.forEach((k, v) -> {
//            sendShopListToCloud(k, v);
//            System.out.printf("%s synchronized: %s%n", k, v);
//        });
//    }

    @SneakyThrows
    public boolean deleteFromDisk(String hashId) {
        return Paths.get(dataDir, hashId).toFile().delete();
    }

    @SneakyThrows
    public void writeOnDisk(String hashId) {
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writer().writeValue(Paths.get(dataDir, hashId).toFile(), localShoppLists.get(hashId));
    }

    public String newShoppList() {
        Optional<HttpResponse<Buffer>> response = cloudRestAdapter.sendSync(SERVER_HOST, HttpMethod.PUT, "/api/shopp/list");
        String hashId;
        if (response.isPresent() && response.get().statusCode() == 200) {
            hashId = response.get().bodyAsString();
        } else {
            hashId = HashUtils.getRandomHash();
        }
        localShoppLists.put(hashId, new ORMap());
        writeOnDisk(hashId);
        return hashId;
    }

    public void deleteShoppList(String hashId) {
        localShoppLists.remove(hashId);

        Optional<HttpResponse<Buffer>> response = cloudRestAdapter.
                sendSync(SERVER_HOST, HttpMethod.DELETE, String.format("/api/shopp/list/%s", hashId));
        if (response.isPresent() && response.get().statusCode() == 200) {
            System.out.printf("Shopping list %s removed from cloud%n", hashId);
        }
        if (deleteFromDisk(hashId)) {
            System.out.printf("Shopping list %s removed from local storage%n", hashId);
        }
    }

    @SneakyThrows
    public ORMap getShoppList(String hashId) {
        localShoppLists.putIfAbsent(hashId, new ORMap());
        ORMap shoppList = localShoppLists.get(hashId);
        sendShopListToCloud(hashId, shoppList);
        Optional<HttpResponse<Buffer>> response = cloudRestAdapter.
                sendSync(SERVER_HOST, HttpMethod.GET, String.format("/api/shopp/list/%s", hashId));
        if (response.isPresent() && response.get().statusCode() == 200) {
            ORMap cloudShoppList = mapper.readValue(response.get().bodyAsString(), ORMap.class);
            localShoppLists.put(hashId, new ORMap(cloudShoppList));
        }
        System.out.printf("Result: %s %s%n", hashId, localShoppLists.get(hashId));
        writeOnDisk(hashId);
        return localShoppLists.get(hashId);
    }

    private void sendShopListToCloud(String hashId, ORMap shoppList) {
        cloudRestAdapter.sendSync(SERVER_HOST, HttpMethod.POST, String.format("/api/shopp/list/%s", hashId), shoppList);
    }

    public void incOrDecItem(String hashId, String name, String quantity, boolean isInc) {
        if (isInc) {
            localShoppLists.get(hashId).inc(name, Integer.valueOf(quantity));
        } else {
            localShoppLists.get(hashId).dec(name, Integer.valueOf(quantity));
        }
        getShoppList(hashId);
    }

    public void incItem(String hashId, String name, String quantity) {
        incOrDecItem(hashId, name, quantity, true);
    }

    public void decItem(String hashId, String name, String quantity) {
        incOrDecItem(hashId, name, quantity, false);
    }

    public void deleteItem(String hashId, String name) {
        localShoppLists.get(hashId).remove(name);
        getShoppList(hashId);
    }
}
