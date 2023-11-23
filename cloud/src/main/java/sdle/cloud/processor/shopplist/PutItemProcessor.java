package sdle.cloud.processor.shopplist;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.ZMQAdapter;

import java.util.List;
import java.util.Map;

public class PutItemProcessor extends BaseShoppListProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("PUT ITEM process %s%n", msg);
        String listHashId = msg.get(2);
        String owner = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", owner, node.getIp(), owner.equals(node.getIp()));
        String reply;
        if (owner.equals(node.getIp())) {
            Map<String, Integer> shoppList = cluster.getShoppLists().get(listHashId);
            if (shoppList == null) {
                reply = REPLY_NOT_FOUND;
            } else {
                shoppList.put(msg.get(3), Integer.valueOf(msg.get(4)));
                System.out.println(cluster.getShoppLists());
                reply = REPLY_OK;
            }
        } else {
            reply = zmqAdapter.sendMsg(owner, node.getClusterPort(), CommandEnum.PUT_ITEM, msg.subList(2, msg.size()));
        }
        zmqAdapter.sendReply(serverSocket, msg, reply);
        return reply;
    }
}
