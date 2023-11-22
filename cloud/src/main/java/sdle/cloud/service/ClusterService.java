package sdle.cloud.service;

import lombok.SneakyThrows;
import org.json.JSONObject;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.ZMQUtils;
import sun.misc.Signal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClusterService extends BaseService {
    public static final String REPLY_OK = "OK";
    private static final Integer MAX_HEARTBEAT_FAILURES = 2;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
    private final Map<String, Integer> heartbeatFailures = new ConcurrentHashMap<>();
    private ZMQ.Socket clusterClientSocket;

    public ClusterService(Node node, Cluster cluster) {
        super(node, cluster);
    }

    @Override
    public void init() {
        super.init();
        clusterClientSocket = ZMQUtils.newClientSocket(context);

        getCluster().getNodes().put(getNode().getId(), getNode().getIp());
        getCluster().updateClusterHashNodes();
        scheduledExecutor.schedule(this::joinCluster, 2, TimeUnit.SECONDS);
        scheduledExecutor.scheduleWithFixedDelay(this::heartBeat, 5, 5, TimeUnit.SECONDS);

        //Signal.handle(new Signal("TERM"), signal -> onInterrupt());
        Signal.handle(new Signal("INT"), signal -> onInterrupt());
    }

    private void heartBeat() {
        System.out.println(getCluster().getNodes());
        getCluster().getNodes().forEach((id, ip) -> {
            if (!getNode().getId().equals(id)) {
                String reply = ZMQUtils.sendMsg(clusterClientSocket, (String) ip, getNode().getClusterPort(), CommandEnum.CLUSTER_HEARTBEAT, Collections.emptyList());
                if (REPLY_OK.equals(reply)) {
                    heartbeatFailures.remove(id);
                } else {
                    heartbeatFailures.merge(id, 1, Integer::sum);
//                    heartbeatFailures.putIfAbsent(id, 0);
//                    heartbeatFailures.put(id, heartbeatFailures.get(getNode().getId()) + 1);
                    if (heartbeatFailures.get(id) > MAX_HEARTBEAT_FAILURES) {
                        getCluster().getNodes().remove(id);
                        getCluster().updateClusterHashNodes();
                        heartbeatFailures.remove(id);
                        ZMQUtils.notifyClusterNodesUpdate(clusterClientSocket, getCluster(), getNode());
                    }
                }
            }
        });
        System.out.printf(" heartbeat failures: %s%n", heartbeatFailures);
    }

    private void joinCluster() {
        JSONObject nodeJson = new JSONObject();
        nodeJson.put(getNode().getId(), getNode().getIp());
        String nextBootstrapHostAddr = getNextBootstrapHostAddr();
        ZMQUtils.sendMsg(clusterClientSocket, nextBootstrapHostAddr, getNode().getClusterPort(), CommandEnum.CLUSTER_JOIN, Collections.singletonList(nodeJson.toString()));
        System.out.println("Node added to cluster");
    }

    @Override
    protected String getServicePort() {
        return getNode().getClusterPort();
    }

    @SneakyThrows
    protected void onInterrupt() {
        System.out.println("terminating and leaving cluster");
        getCluster().getNodes().values().forEach(ip -> {
            if (!ip.equals(getNode().getIp())) {
                JSONObject nodeJson = new JSONObject();
                nodeJson.put(getNode().getId(), getNode().getIp());
                ZMQUtils.sendMsg(clusterClientSocket, getNextBootstrapHostAddr(), getNode().getClusterPort(), CommandEnum.CLUSTER_LEAVE, Collections.singletonList(nodeJson.toString()));
            }
        });
        System.exit(0);
    }

    private String getNextBootstrapHostAddr() {
        String hostAddr = getNode().getIp();
        while (Objects.equals(hostAddr, getNode().getIp())) {
            hostAddr = getNode().getBootstrapList().get(getCluster().getNextBootstrapHost());
            getCluster().setNextBootstrapHost((getCluster().getNextBootstrapHost() + 1) % getNode().getBootstrapList().size());
        }
        return hostAddr;
    }

    @Override
    protected void processMsg(List<String> msg) {
        System.out.printf("processing cluster msg: %s%n", msg);
        CommandEnum messageEnum = CommandEnum.getCommand(msg.get(1));
        messageEnum.getProcessor().process(getSocket(), clusterClientSocket, msg, getCluster(), getNode());
    }
}
