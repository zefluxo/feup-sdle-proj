package sdle.cloud.processor.shopplist;

import sdle.cloud.cluster.Cluster;
import sdle.cloud.processor.BaseProcessor;

public abstract class BaseShoppListProcessor extends BaseProcessor {
    protected String getListOwner(Cluster cluster, String listHashId) {
        for (String nodeHash : cluster.getNodeHashes().keySet()) {
            System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> : %s, %s, %s%n", listHashId, nodeHash, listHashId.compareTo(nodeHash) < 0);
            if (listHashId.compareTo(nodeHash) < 0) {
                return cluster.getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o primeiro node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        return cluster.getNodeHashes().firstEntry().getValue();
    }

}
