package sdle.cloud.processor.shopplist;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.HashUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PutListProcessor extends BaseShoppListProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node) {
        //
        String listHashId;
        if (msg.size() > 3) {
            listHashId = msg.get(3);
        } else {
            listHashId = HashUtils.getRandomHash();
        }
        String dest = "";
        for (String nodeHash : cluster.getNodeHashes().keySet()) {
            System.out.printf(listHashId, nodeHash, listHashId.compareTo(nodeHash) > 0);
            if (listHashId.compareTo(nodeHash) > 0) {
                dest = cluster.getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o ultimo node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        if (dest.isEmpty()) dest = cluster.getNodeHashes().lastEntry().getValue();
        System.out.printf("%s, %s, %s%n", dest, node.getIp(), dest.equals(node.getIp()));
        if (dest.equals(node.getIp())) {
            cluster.getShoppLists().put(listHashId, new HashMap<>());
            System.out.println(cluster.getShoppLists());
        } else {
            sendMsg(clientSocket, dest, node.getClusterPort(), CommandEnum.PUT_LIST, Collections.singletonList(listHashId));
        }
        sendReply(serverSocket, msg, cluster, node, listHashId);
        return listHashId;
    }

}
