package sdle.cloud.processor.shopplist;

import org.json.JSONObject;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.processor.BaseProcessor;
import sdle.cloud.utils.ZMQAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BaseShoppListProcessor extends BaseProcessor {
    public static final int REPLICATE_FACTOR = 3;

    protected String getListOwner(Cluster cluster, String listHashId) {
        for (String nodeHash : cluster.getNodeHashes().keySet()) {
            //System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> : %s, %s, %s%n", listHashId, nodeHash, listHashId.compareTo(nodeHash) < 0);
            if (listHashId.compareTo(nodeHash) < 0) {
                return cluster.getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o primeiro node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        return cluster.getNodeHashes().firstEntry().getValue();
    }

    protected void sendReplicateList(ZMQAdapter zmqAdapter, Cluster cluster, Node node, String listHashId, Map<String, Integer> shoppList) {
        List<String> replicaHashes = getReplicateHashes(cluster, node);

        replicaHashes.forEach(hash -> {
            List<String> params = new ArrayList<>();
            params.add(listHashId);
            params.add(new JSONObject(shoppList).toString());
            zmqAdapter.sendMsg(cluster.getNodeHashes().get(hash), node.getClusterPort(), CommandEnum.REPLICATE_LIST, params);
        });
    }

    protected List<String> getReplicateHashes(Cluster cluster, Node node) {
        Iterator<String> iterator = cluster.getNodeHashes().keySet().iterator();
        String next = "";
        List<String> replicateHashes = new ArrayList<>();
        while (iterator.hasNext()) {
            next = iterator.next();
            if (next.equals(node.getHashId())) {
                for (int i = 1; i < (Math.min(REPLICATE_FACTOR, cluster.getNodeHashes().size())); i++) {
                    if (!iterator.hasNext()) {
                        iterator = cluster.getNodeHashes().keySet().iterator();
                    }
                    replicateHashes.add(iterator.next());
                }
                break;
            }
        }
        return replicateHashes;
    }

}
