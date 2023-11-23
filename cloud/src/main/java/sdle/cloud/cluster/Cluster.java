package sdle.cloud.cluster;

import lombok.Data;
import sdle.cloud.utils.HashUtils;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Cluster {

    // TODO: a ideia eh depois usar aqui um "CRDMap", simplificando a questao da concorrencia
    private final Map<String, Map<String, Integer>> shoppLists = new ConcurrentHashMap<>();
    private Map<String, Object> nodes = new ConcurrentHashMap<>();
    private int nextBootstrapHost = 0;
    private TreeMap<String, String> nodeHashes = new TreeMap<>();

    public void updateClusterHashNodes() {
        nodeHashes.clear();
        nodes.forEach((k, v) -> nodeHashes.put(HashUtils.getHash(k), (String) v));
        System.out.println(nodeHashes);
    }

    public void printStatus(Node node) {
        System.out.printf("[%s, %s] Nodes:          %s%n", node.getHashId(), node.getIp(), nodes);
        System.out.printf("[%s, %s] Node hashes:      %s%n", node.getHashId(), node.getIp(), nodeHashes);
        System.out.printf("[%s, %s] Shopping Lists: %s%n", node.getHashId(), node.getIp(), shoppLists);
    }
}
