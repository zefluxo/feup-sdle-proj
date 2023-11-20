package sdle.cloud.processor.shopplist;

import sdle.cloud.cluster.Cluster;
import sdle.cloud.processor.BaseProcessor;

public abstract class BaseShoppListProcessor extends BaseProcessor {
    protected String getListDestination(Cluster cluster, String listHashId) {
        String dest = "";
        for (String nodeHash : cluster.getNodeHashes().keySet()) {
            if (listHashId.compareTo(nodeHash) < 0) {
                dest = cluster.getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o ultimo node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        if (dest.isEmpty()) dest = cluster.getNodeHashes().firstEntry().getValue();
        return dest;
    }

}
