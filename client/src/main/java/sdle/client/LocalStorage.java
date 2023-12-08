package sdle.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import sdle.crdt.implementations.ORMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class LocalStorage {
    private final String dataDir = System.getProperty("user.dir") + "/data";
    private final Map<String, ORMap> localShoppLists = new HashMap<>();
    private final CloudRestAdapter cloudRestAdapter = new CloudRestAdapter();

    public LocalStorage() {
        synchronise();
    }

    //    ObjectMapper mapper = new ObjectMapper();
//    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
    @SneakyThrows
    public void readAllFromDisk() {

        List<Path> paths = Files.walk(Paths.get(dataDir), 1)
                .filter(Files::isRegularFile)
                .toList();
        for (Path path : paths) {
            localShoppLists.put(path.getFileName().toString(), new ObjectMapper().readValue(new String(Files.readAllBytes(path)), ORMap.class));
        }
    }

    public void synchronise() {
        System.out.println("Synchronizing lists from cloud");
        readAllFromDisk();
        localShoppLists.forEach((k, v) -> {
            v.join(cloudRestAdapter.getShoppList(k));
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
}
