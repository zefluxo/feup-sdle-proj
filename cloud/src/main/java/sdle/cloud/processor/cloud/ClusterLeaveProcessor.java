package sdle.cloud.processor.cloud;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.ZMQAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClusterLeaveProcessor extends BaseCloudProcessor {
    @Override
    public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        System.out.printf("LEAVE process %s%n", msg);

        node.setMaintenance(true);

        cluster.getNodes().remove(node.getId());
        cluster.getNodeHashes().remove(node.getHashId());
        JSONObject nodeJson = new JSONObject();
        nodeJson.put(node.getId(), node.getIp());
        String leavingReply = zmqAdapter.sendMsg((String) cluster.getNodes().values().stream().findFirst().get(), node.getClusterPort(), CommandEnum.CLUSTER_LEAVING, Collections.singletonList(nodeJson.toString()));

        cluster.getShoppLists().forEach((shoppListHashId, shoppList) -> {
            String ownerIp = getListOwner(cluster, shoppListHashId);
            List<String> params = new ArrayList<>();
            params.add(shoppListHashId);
            params.add(new JSONObject(shoppList).toString());
            zmqAdapter.sendMsg(ownerIp, node.getClusterPort(), CommandEnum.PUT_LIST, params);
        });

        cluster.getReplicateShoppLists().forEach((shoppListHashId, shoppList) -> {
            String ownerIp = getListOwner(cluster, shoppListHashId);
            List<String> params = new ArrayList<>();
            params.add(shoppListHashId);
            params.add(new JSONObject(shoppList).toString());
            zmqAdapter.sendMsg(ownerIp, node.getClusterPort(), CommandEnum.PUT_LIST, params);
        });

        reply(serverSocket, zmqAdapter, msg, cluster, node, leavingReply, false);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);

        return leavingReply;

    }
}
