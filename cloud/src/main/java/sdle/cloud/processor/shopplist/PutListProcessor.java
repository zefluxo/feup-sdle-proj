package sdle.cloud.processor.shopplist;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.HashUtils;
import sdle.cloud.utils.ZMQAdapter;

import java.util.*;

public class PutListProcessor extends BaseShoppListProcessor {

    private static void onPutList(ZMQAdapter zmqAdapter, Cluster cluster, Node node, String listHashId, Map<String, Integer> shoppList) {
        cluster.getShoppLists().put(listHashId, new HashMap<>());

        List<String> replicaHashes = getReplicateHashes(cluster, node);

        replicaHashes.forEach(hash -> {
            List<String> params = new ArrayList<>();
            params.add(listHashId);
            params.add(new JSONObject(shoppList).toString());
            zmqAdapter.sendMsg(cluster.getNodeHashes().get(hash), node.getClusterPort(), CommandEnum.REPLICATE_LIST, params);
        });
        System.out.println(cluster.getShoppLists());
    }

    private static List<String> getReplicateHashes(Cluster cluster, Node node) {
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

    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("PUT LIST process %s%n", msg);
        String listHashId;
        if (msg.size() > 2) {
            listHashId = msg.get(2);
        } else {
            listHashId = String.valueOf(HashUtils.getRandomHash());
        }
        String ownerIp = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()));
        if (ownerIp.equals(node.getIp())) {
            onPutList(zmqAdapter, cluster, node, listHashId, new HashMap<>());
        } else {
            zmqAdapter.sendMsg(ownerIp, node.getClusterPort(), CommandEnum.PUT_LIST, Collections.singletonList(listHashId));
        }
        zmqAdapter.sendReply(serverSocket, msg, listHashId);
        return listHashId;
    }
}
