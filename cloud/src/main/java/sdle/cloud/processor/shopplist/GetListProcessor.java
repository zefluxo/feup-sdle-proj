package sdle.cloud.processor.shopplist;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.ZMQAdapter;

import java.util.Collections;
import java.util.List;

public class GetListProcessor extends BaseShoppListProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("GET LIST process %s%n", msg);
        String listHashId = msg.get(2);
        String owner = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", owner, node.getIp(), owner.equals(node.getIp()));
        String reply;
        if (owner.equals(node.getIp())) {
            reply = String.valueOf(cluster.getShoppLists().get(listHashId));
            if ("null".equals(reply)) {
                reply = REPLY_NOT_FOUND;
            }
        } else {
            if (cluster.getReplicateShoppLists().containsKey(listHashId)) {
                reply = String.valueOf(cluster.getReplicateShoppLists().get(listHashId));
            } else {
                reply = zmqAdapter.sendMsg(owner, node.getClusterPort(), CommandEnum.GET_LIST, Collections.singletonList(listHashId));
            }
        }
        zmqAdapter.sendReply(serverSocket, msg, reply);
        return reply;
    }

}
