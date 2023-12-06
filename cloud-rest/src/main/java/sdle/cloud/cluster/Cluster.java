package sdle.cloud.cluster;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import lombok.Data;
import sdle.cloud.utils.HashUtils;
import sdle.crdt.implementations.ORMap;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Singleton
public class Cluster {

    // TODO: a ideia eh depois usar aqui um "CRDMap", simplificando a questao da concorrencia
    Map<String, ORMap> shoppLists = new ConcurrentHashMap<>();
    Map<String, ORMap> replicateShoppLists = new ConcurrentHashMap<>();

    Map<String, Object> nodes = new ConcurrentHashMap<>();
    TreeMap<String, String> nodeHashes = new TreeMap<>();

    int nextBootstrapHost = 0;


    Cluster() {
        //
    }

    void onStart(@Observes StartupEvent ev) {
        System.out.println("Cluster startup");
//        shoppLists = new ConcurrentHashMap<>();
//        replicateShoppLists = new ConcurrentHashMap<>();
//        nodes = new ConcurrentHashMap<>();
//        nodeHashes = new TreeMap<>();
    }


    public void updateClusterHashNodes() {
        nodeHashes.clear();
        nodes.forEach((k, v) -> nodeHashes.put(HashUtils.getHash(k), (String) v));
        System.out.println(nodeHashes);
    }

    public void printStatus(Node node) {
        System.out.printf("[%s, %s] Nodes:          %s%n", node.getHashId(), node.getIp(), nodes);
        System.out.printf("[%s, %s] Node hashes:      %s%n", node.getHashId(), node.getIp(), nodeHashes);
        System.out.printf("[%s, %s] Shopping Lists: %s%n", node.getHashId(), node.getIp(), shoppLists);
        System.out.printf("[%s, %s] Replicate Shopping Lists: %s%n", node.getHashId(), node.getIp(), replicateShoppLists);
    }
}
