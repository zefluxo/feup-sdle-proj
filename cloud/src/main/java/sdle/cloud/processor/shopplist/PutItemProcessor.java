package sdle.cloud.processor.shopplist;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;

import java.util.List;
import java.util.Map;

public class PutItemProcessor extends BaseShoppListProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node) {
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
            Map<String, Integer> shoppList = cluster.getShoppLists().get(listHashId);
            if (shoppList == null) {
                reply = REPLY_NOT_FOUND;
            } else {
                shoppList.put(msg.get(4), Integer.valueOf(msg.get(5)));
                System.out.println(cluster.getShoppLists());
                reply = REPLY_OK;
            }
        } else {
            reply = sendMsg(clientSocket, dest, node.getClusterPort(), CommandEnum.PUT_ITEM, msg.subList(3, msg.size()));
        }
        sendReply(serverSocket, msg, cluster, node, reply);
        return reply;
    }
}
