package sdle.cloud.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    public FileUtils() {
        //
    }

    @PostConstruct
    @SneakyThrows
    public void onStart() {
        String dataDir = Paths.get(nodeConfiguration.getDataDir()).toString();
        //boolean mkdir = Paths.get(dataDir).toFile().mkdirs();
        shoppListsDir = Paths.get(dataDir, "shoppLists");
        replicateShoppListsDir = Paths.get(dataDir, "replicateShoppLists");
        nodePath = Paths.get(dataDir, "node");
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
                .filter(Files::isRegularFile)) {
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
        writeList(replicateShoppListsDir, hashId, shoppList);
    }

    public boolean deleteShoppList(String hashId) {
        return Paths.get(shoppListsDir.toString(), hashId).toFile().delete();
    }

    public boolean deleteReplicateShoppList(String hashId) {
        return Paths.get(replicateShoppListsDir.toString(), hashId).toFile().delete();
    }


    @SneakyThrows
    public void writeList(Path dir, String hashId, ORMap shoppList) {
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writer().writeValue(Paths.get(dir.toString(), hashId).toFile(), shoppList);
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
