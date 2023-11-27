package sdle.cloud.processor.shopplist;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.HashUtils;
import sdle.cloud.utils.ZMQAdapter;

import java.util.ArrayList;
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
        HashMap<String, Integer> shoppList = new HashMap<>();
        if (msg.size() > 3) {
            new JSONObject(msg.get(3)).toMap().forEach((k, v) -> {
                shoppList.put(k, (Integer) v);
            });
        }
        String ownerIp = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s, %s %n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()), shoppList);
        if (ownerIp.equals(node.getIp())) {
            cluster.getShoppLists().put(listHashId, shoppList);
            cluster.getReplicateShoppLists().remove(listHashId);
            sendReplicateList(zmqAdapter, cluster, node, listHashId, shoppList);
            System.out.println(cluster.getShoppLists());
        } else {
            List<String> params = new ArrayList<>();
            params.add(listHashId);
            if (!shoppList.isEmpty()) {
                params.add(new JSONObject(shoppList).toString());
            }
            zmqAdapter.sendMsg(ownerIp, node.getClusterPort(), CommandEnum.PUT_LIST, params);
        }
        zmqAdapter.sendReply(serverSocket, msg, listHashId);
        return listHashId;
    }
}
