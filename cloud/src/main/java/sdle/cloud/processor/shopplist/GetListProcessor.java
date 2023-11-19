package sdle.cloud.processor.shopplist;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;

import java.util.Collections;
import java.util.List;

public class GetListProcessor extends BaseShoppListProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node) {
        //
        String listHashId = msg.get(3);
        String dest = "";
        for (String nodeHash : cluster.getNodeHashes().keySet()) {
            if (listHashId.compareTo(nodeHash) > 0) {
                dest = cluster.getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o ultimo node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        if (dest.isEmpty()) dest = cluster.getNodeHashes().lastEntry().getValue();
        System.out.printf("%s, %s, %s%n", dest, node.getIp(), dest.equals(node.getIp()));
        String reply;
        if (dest.equals(node.getIp())) {
            reply = String.valueOf(cluster.getShoppLists().get(listHashId));
            if ("null".equals(reply)) {
                reply = REPLY_NOT_FOUND;
            }
        } else {
            reply = sendMsg(clientSocket, dest, node.getClusterPort(), CommandEnum.GET_LIST, Collections.singletonList(listHashId));
        }
        sendReply(serverSocket, msg, cluster, node, reply);
        return reply;
    }
}
