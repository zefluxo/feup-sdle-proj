package sdle.cloud.processor.shopplist;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.ZMQUtils;

import java.util.Collections;
import java.util.List;

public class GetListProcessor extends BaseShoppListProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("GET LIST process %s%n", msg);
        String listHashId = msg.get(2);
        String dest = getListDestination(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", dest, node.getIp(), dest.equals(node.getIp()));
        String reply;
        if (dest.equals(node.getIp())) {
            reply = String.valueOf(cluster.getShoppLists().get(listHashId));
            if ("null".equals(reply)) {
                reply = REPLY_NOT_FOUND;
            }
        } else {
            reply = ZMQUtils.sendMsg(clientSocket, dest, node.getClusterPort(), CommandEnum.GET_LIST, Collections.singletonList(listHashId));
        }
        ZMQUtils.sendReply(serverSocket, msg, reply);
        return reply;
    }

}
