package sdle.cloud.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import sdle.cloud.NodeConfiguration;
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

    @Inject
    public NodeConfiguration nodeConfiguration;

    private Path shoppListsDir;
    private Path replicateShoppListsDir;
    private Path nodePath;
    //private static final Path DATA_DIR = Paths.get(System.getProperty("sdle.cloud.dataDir", "data"));
//
//    private static final Path SHOPP_LISTS_DIR = Paths.get(DATA_DIR.toString(), "shoppLists");
//    private static final Path REPLICATE_SHOPP_LISTS_DIR = Paths.get(DATA_DIR.toString(), "replicateShoppLists");
//
//    private static final Path NODE_PATH = Paths.get(DATA_DIR.toString(), "node");

    public FileUtils() {
        //
    }

    @PostConstruct
    public void onStart() {
        shoppListsDir = Paths.get(nodeConfiguration.getDataDir(), "shoppLists");
        replicateShoppListsDir = Paths.get(nodeConfiguration.getDataDir(), "replicateShoppLists");
        nodePath = Paths.get(nodeConfiguration.getDataDir(), "node");
        System.out.printf("Creating data dirs (post-construct): %s%n%s, %s%n",
                nodeConfiguration.getDataDir(),
                shoppListsDir.toFile().mkdirs(),
                replicateShoppListsDir.toFile().mkdirs());
    }

    public Map<String, ORMap> readShoppLists() {
        return readLists(shoppListsDir);
    }

    public Map<String, ORMap> readReplicateShoppLists() {
        return readLists(replicateShoppListsDir);
    }

    @SneakyThrows
    public Map<String, ORMap> readLists(Path dir) {
        Map<String, ORMap> shoppLists = new HashMap<>();
        try (Stream<Path> pathsStream = Files.walk(dir, 1)
                .filter(Files::isRegularFile).onClose(() -> System.out.println("The Stream is closed"))) {
            for (Path path : pathsStream.toList()) {
                shoppLists.put(path.getFileName().toString(), new ObjectMapper().readValue(Files.readString(path), ORMap.class));
            }
        }
        return shoppLists;
    }

    public void writeShoppList(String hashId, ORMap shoppList) {
        writeList(shoppListsDir, hashId, shoppList);
    }

    public void writeReplicateShoppList(String hashId, ORMap shoppList) {
        writeList(shoppListsDir, hashId, shoppList);
    }

    @SneakyThrows
    public void writeList(Path dir, String hashId, ORMap shoppList) {
        new ObjectMapper().writer().writeValue(Paths.get(dir.toString(), hashId).toFile(), shoppList);
    }

    @SneakyThrows
    public Node readNode() {
        return new ObjectMapper().readValue(Files.readString(nodePath), Node.class);
    }

    @SneakyThrows
    public void writeNode(Node node) {
        new ObjectMapper().writer().writeValue(nodePath.toFile(), node);
    }
}
