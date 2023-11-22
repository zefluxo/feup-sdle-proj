package sdle.cloud.processor.cloud;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.processor.BaseProcessor;
import sdle.cloud.utils.ZMQUtils;

import java.util.List;

public abstract class BaseCloudProcessor extends BaseProcessor {


    protected void reply(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node, String reply, boolean needsNotify) {
        ZMQUtils.sendReply(serverSocket, msg, reply);
        if (needsNotify) ZMQUtils.notifyClusterNodesUpdate(clientSocket, cluster, node);
    }
}
