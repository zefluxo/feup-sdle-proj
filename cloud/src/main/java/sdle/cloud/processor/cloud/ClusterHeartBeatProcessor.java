package sdle.cloud.processor.cloud;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.ZMQAdapter;

import java.util.List;

public class ClusterHeartBeatProcessor extends BaseCloudProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("HEARTBEAT process %s%n", msg);
        String reply = REPLY_OK;
        reply(serverSocket, zmqAdapter, msg, cluster, node, reply, false);
        return reply;
    }
}
