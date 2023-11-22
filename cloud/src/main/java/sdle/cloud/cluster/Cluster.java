package sdle.cloud.cluster;

import lombok.Data;
import sdle.cloud.utils.HashUtils;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Cluster {

    private final Map<String, Map<String, Integer>> shoppLists = new ConcurrentHashMap<>();
    // TODO: a ideia eh depois usar aqui um "CRDMap", simplificando a questao da concorrencia
    private Map<String, Object> nodes = new ConcurrentHashMap<>();
    private int nextBootstrapHost = 0;
    private TreeMap<String, String> nodeHashes = new TreeMap<>();

    public void updateClusterHashNodes() {
        nodeHashes.clear();
        nodes.forEach((k, v) -> nodeHashes.put(HashUtils.getHash(k), (String) v));
        System.out.println(nodeHashes);
    }
}
