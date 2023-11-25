package sdle.cloud.processor.shopplist;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.ZMQAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicateListProcessor extends BaseShoppListProcessor {

    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("REPLICATE LIST process %s%n", msg);
        String listHashId = msg.get(2);
        Map<String, Integer> replicateList = new ConcurrentHashMap<>();
        new JSONObject(msg.get(3)).toMap().forEach((k, v) -> {
            replicateList.put(k, (Integer) v);
        });

        cluster.getReplicateShoppLists().put(listHashId, replicateList);

        zmqAdapter.sendReply(serverSocket, msg, listHashId);
        return listHashId;
    }
}
