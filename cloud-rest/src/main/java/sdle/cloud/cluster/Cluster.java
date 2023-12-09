package sdle.cloud.cluster;

import io.quarkus.runtime.ShutdownEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;
import sdle.cloud.utils.FileUtils;
import sdle.cloud.utils.HashUtils;
import sdle.crdt.implementations.ORMap;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Singleton
public class Cluster {

    Map<String, ORMap> shoppLists;
    Map<String, ORMap> replicateShoppLists;

    Map<String, Object> nodes = new ConcurrentHashMap<>();
    TreeMap<String, String> nodeHashes = new TreeMap<>();

    int nextBootstrapHost = 0;

    @Inject
    FileUtils fileUtils;

    Cluster() {
        //
    }

    @PostConstruct
    void onStart() {
        System.out.println("Cluster startup");
        shoppLists = fileUtils.readShoppLists();
        replicateShoppLists = fileUtils.readReplicateShoppLists();
    }

    void onStop(@Observes ShutdownEvent ev) {
        System.out.println("Cluster shutdown");
        writeListsOnDisk();
    }

    public void updateClusterHashNodes() {
        nodeHashes.clear();
        nodes.forEach((k, v) -> nodeHashes.put(HashUtils.getHash(k), (String) v));
        System.out.println(nodeHashes);
    }

    public void printStatus(Node node) {
        //System.out.printf("[%s, %s] Nodes: %n%s%n", node.getHashId(), node.getIp(), nodes);
        System.out.printf("[%s, %s] Node hashes: %n%s%n", node.getHashId(), node.getIp(), nodeHashes);
        System.out.printf("[%s, %s] Shopping Lists: %n", node.getHashId(), node.getIp());
        printShoppList(shoppLists);
        System.out.printf("[%s, %s] Replicate Shopping Lists: %n", node.getHashId(), node.getIp());
        printShoppList(replicateShoppLists);
    }

    public void writeListsOnDisk() {
        shoppLists.forEach((k, v) -> fileUtils.writeShoppList(k, v));
        replicateShoppLists.forEach((k, v) -> fileUtils.writeReplicateShoppList(k, v));
    }

    private void printShoppList(Map<String, ORMap> shoppLists) {
        if (shoppLists.isEmpty()) return;
        StringBuilder result = new StringBuilder();
        shoppLists.forEach((hash, shoppList) -> result.append(String.format("    %s: %s%n", hash, shoppList.getMap())));
        System.out.println(result);
    }
}
