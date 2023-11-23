package sdle.cloud.processor.shopplist;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.HashUtils;
import sdle.cloud.utils.ZMQAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PutListProcessor extends BaseShoppListProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("PUT LIST process %s%n", msg);
        String listHashId;
        if (msg.size() > 2) {
            listHashId = msg.get(2);
        } else {
            listHashId = String.valueOf(HashUtils.getRandomHash());
        }
        String ownerIp = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()));
        if (ownerIp.equals(node.getIp())) {
            cluster.getShoppLists().put(listHashId, new HashMap<>());
            System.out.println(cluster.getShoppLists());
        } else {
            zmqAdapter.sendMsg(ownerIp, node.getClusterPort(), CommandEnum.PUT_LIST, Collections.singletonList(listHashId));
        }
        zmqAdapter.sendReply(serverSocket, msg, listHashId);
        return listHashId;
    }

}
