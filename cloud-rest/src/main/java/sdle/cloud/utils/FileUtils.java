package sdle.cloud.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import sdle.cloud.cluster.Node;
import sdle.crdt.implementations.ORMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Singleton
public class FileUtils {

    private static final Path DATA_DIR = Paths.get(System.getProperty("sdle.cloud.dataDir", "data"));

    private static final Path SHOPP_LISTS_DIR = Paths.get(DATA_DIR.toString(), "shoppLists");
    private static final Path REPLICATE_SHOPP_LISTS_DIR = Paths.get(DATA_DIR.toString(), "replicateShoppLists");

    private static final Path NODE_PATH = Paths.get(DATA_DIR.toString(), "node");

    public Map<String, ORMap> readShoppLists() {
        return readLists(SHOPP_LISTS_DIR);
    }

    public Map<String, ORMap> readReplicateShoppLists() {
        return readLists(REPLICATE_SHOPP_LISTS_DIR);
    }

    void onStart(@Observes StartupEvent ev) {
        DATA_DIR.toFile().mkdirs();
        SHOPP_LISTS_DIR.toFile().mkdirs();
        REPLICATE_SHOPP_LISTS_DIR.toFile().mkdirs();
    }

    @SneakyThrows
    public Map<String, ORMap> readLists(Path dir) {
        Map<String, ORMap> shoppLists = new HashMap<>();
        try (Stream<Path> pathsStream = Files.walk(dir, 1)
                .filter(Files::isRegularFile).onClose(() -> System.out.println("The Stream is closed"))) {
            for (Path path : pathsStream.toList()) {
                shoppLists.put(path.getFileName().toString(), new ObjectMapper().readValue(new String(Files.readAllBytes(path)), ORMap.class));
            }
        }
        return shoppLists;
    }

    public void writeShoppList(String hashId, ORMap shoppList) {
        writeList(SHOPP_LISTS_DIR, hashId, shoppList);
    }

    public void writeReplicateShoppList(String hashId, ORMap shoppList) {
        writeList(REPLICATE_SHOPP_LISTS_DIR, hashId, shoppList);
    }

    @SneakyThrows
    public void writeList(Path dir, String hashId, ORMap shoppList) {
        new ObjectMapper().writer().writeValue(Paths.get(dir.toString(), hashId).toFile(), shoppList);
    }

    @SneakyThrows
    public Node readNode() {
        return new ObjectMapper().readValue(new String(Files.readAllBytes(NODE_PATH)), Node.class);
    }

    @SneakyThrows
    public void writeNode(Node node) {
        new ObjectMapper().writer().writeValue(NODE_PATH.toFile(), node);
    }
}
