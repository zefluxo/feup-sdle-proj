package sdle.cloud.service;

import lombok.SneakyThrows;
import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sun.misc.Signal;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClusterService extends BaseService {
    public static final String REPLY_OK = "OK";
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private ZMQ.Socket clusterClientSocket;

    public ClusterService(Node node, Cluster cluster) {
        super(node, cluster);
    }

    @Override
    public void init() {
        super.init();
        clusterClientSocket = context.createSocket(SocketType.DEALER);
        clusterClientSocket.setIdentity((Thread.currentThread().getName() + new Random().nextInt(1000)).getBytes(ZMQ.CHARSET));

        getCluster().getNodes().put(getNode().getId(), getNode().getIp());
        getCluster().updateClusterHashNodes();
        scheduledExecutor.schedule(this::joinCluster, 2, TimeUnit.SECONDS);
        scheduledExecutor.scheduleWithFixedDelay(this::printStatus, 10, 30, TimeUnit.SECONDS);

        //Signal.handle(new Signal("TERM"), signal -> onInterrupt());
        Signal.handle(new Signal("INT"), signal -> onInterrupt());
    }

    private void printStatus() {
        System.out.println(getCluster().getNodes());
        System.out.println(getCluster().getNodeHashes());
    }

    private void joinCluster() {
        JSONObject nodeJson = new JSONObject();
        nodeJson.put(getNode().getId(), getNode().getIp());
        CommandEnum.CLUSTER_JOIN.getProcessor().sendMsg(clusterClientSocket, getNextBootstrapHostAddr(), getNode().getClusterPort(), CommandEnum.CLUSTER_JOIN, Collections.singletonList(nodeJson.toString()));
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
            JSONObject nodeJson = new JSONObject();
            nodeJson.put(getNode().getId(), getNode().getIp());
            CommandEnum.CLUSTER_LEAVE.getProcessor().sendMsg(clusterClientSocket, getNextBootstrapHostAddr(), getNode().getClusterPort(), CommandEnum.CLUSTER_LEAVE, Collections.singletonList(nodeJson.toString()));
        });
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
        CommandEnum messageEnum = CommandEnum.getMessage(msg.get(1));
        messageEnum.getProcessor().process(getSocket(), clusterClientSocket, msg, getCluster(), getNode());
    }
}
