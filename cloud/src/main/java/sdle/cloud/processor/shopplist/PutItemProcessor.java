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
        String listHashId = msg.get(2);
        String dest = getListDestination(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", dest, node.getIp(), dest.equals(node.getIp()));
        String reply;
        if (dest.equals(node.getIp())) {
            Map<String, Integer> shoppList = cluster.getShoppLists().get(listHashId);
            if (shoppList == null) {
                reply = REPLY_NOT_FOUND;
            } else {
                shoppList.put(msg.get(3), Integer.valueOf(msg.get(4)));
                System.out.println(cluster.getShoppLists());
                reply = REPLY_OK;
            }
        } else {
            reply = sendMsg(clientSocket, dest, node.getClusterPort(), CommandEnum.PUT_ITEM, msg.subList(2, msg.size()));
        }
        sendReply(serverSocket, msg, cluster, node, reply);
        return reply;
    }
}
