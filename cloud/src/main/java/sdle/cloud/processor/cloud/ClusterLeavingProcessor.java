package sdle.cloud.processor.cloud;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.HashUtils;
import sdle.cloud.utils.ZMQAdapter;

import java.util.List;

public class ClusterLeavingProcessor extends BaseCloudProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("LEAVE process %s%n", msg);
        synchronized (this) {
            String leavingNodeId = new JSONObject(msg.get(2)).keys().next();
            cluster.getNodes().remove(leavingNodeId);
            cluster.getNodeHashes().remove(HashUtils.getHash(leavingNodeId));
        }
        String reply = REPLY_OK;
        reply(serverSocket, zmqAdapter, msg, cluster, node, reply, true);
        return reply;

    }
}
