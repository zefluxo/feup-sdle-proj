package sdle.cloud.processor.cloud;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.processor.BaseProcessor;

import java.util.Collections;
import java.util.List;

public abstract class BaseCloudProcessor extends BaseProcessor {

    protected void notifyClusterNodesUpdate(ZMQ.Socket clientSocket, Cluster cluster, Node node) {
        System.out.printf("updating the cluster %s%n", cluster.getNodes());
        cluster.getNodes().values().forEach(ip -> {
            if (ip != node.getIp()) {
                sendMsg(clientSocket, (String) ip, node.getClusterPort(), CommandEnum.CLUSTER_UPDATE, Collections.singletonList(new JSONObject(cluster.getNodes()).toString()));
            }
        });
    }

    protected void reply(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node, String reply, boolean needsNotify) {
        super.sendReply(serverSocket, msg, cluster, node, reply);
        if (needsNotify) notifyClusterNodesUpdate(clientSocket, cluster, node);
    }
}
