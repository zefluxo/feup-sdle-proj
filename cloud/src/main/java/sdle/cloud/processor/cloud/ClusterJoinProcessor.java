package sdle.cloud.processor.cloud;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;

import java.util.List;

public class ClusterJoinProcessor extends BaseCloudProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("JOIN process %s%n", msg);
        synchronized (this) {
            cluster.getNodes().putAll(new JSONObject(msg.get(2)).toMap());
            cluster.updateClusterHashNodes();
        }
        String reply = REPLY_OK;
        reply(serverSocket, clientSocket, msg, cluster, node, reply, true);
        return reply;
    }
}
