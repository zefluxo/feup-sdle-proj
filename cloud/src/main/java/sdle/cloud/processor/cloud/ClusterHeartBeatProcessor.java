package sdle.cloud.processor.cloud;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;

import java.util.List;

public class ClusterHeartBeatProcessor extends BaseCloudProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("HEARTBEAT process %s%n", msg);
        String reply = REPLY_OK;
        reply(serverSocket, clientSocket, msg, cluster, node, reply, false);
        return reply;
    }
}
