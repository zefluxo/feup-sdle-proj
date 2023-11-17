package sdle.cloud.service;

import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.message.CommandType;
import sun.misc.Signal;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClusterService extends BaseService {
    public static final String REPLY_OK = "OK";
    private final ZContext context = new ZContext();
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    private ZMQ.Socket clusterClientSocket;

    public ClusterService(Node node, Cluster cluster) {
        super(node, cluster);
    }

    @Override
    public void init() {
        super.init();
        clusterClientSocket = context.createSocket(SocketType.REQ);
        clusterClientSocket.setIdentity((Thread.currentThread().getName() + new Random().nextInt(1000)).getBytes(ZMQ.CHARSET));

        getCluster().getNodes().put(getNode().getId(), getNode().getIp());
        getCluster().updateClusterHashNodes();
        scheduledExecutor.schedule(this::joinCluster, 2, TimeUnit.SECONDS);
        scheduledExecutor.scheduleWithFixedDelay(this::printStatus, 10, 30, TimeUnit.SECONDS);

        Signal.handle(new Signal("TERM"), signal -> onInterrupt());
    }

    private void printStatus() {
        System.out.println(getCluster().getNodes());
        System.out.println(getCluster().getHashNodeIds());
    }

    private void joinCluster() {
        JSONObject nodeJson = new JSONObject();
        nodeJson.put(getNode().getId(), getNode().getIp());
        sendMsg(getNextBootstrapHostAddr(), CommandEnum.CLUSTER_JOIN, nodeJson.toString());
        System.out.println("Node added to cluster");
    }

    @Override
    protected String getServicePort() {
        return getNode().getClusterPort();
    }

    protected void onInterrupt() {
        System.out.println("terminating and leaving cluster");
        getCluster().getNodes().values().forEach(ip -> {
            JSONObject nodeJson = new JSONObject();
            nodeJson.put(getNode().getId(), getNode().getIp());
            sendMsg(getNextBootstrapHostAddr(), CommandEnum.CLUSTER_LEAVE, nodeJson.toString());
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

    private String sendMsg(String dest, CommandEnum commandEnum, String msg) {
        System.out.printf("Sending command %s:%s to %s%n", commandEnum, msg, dest);
        String addr = get0MQAddr(dest, getNode().getClusterPort());
        clusterClientSocket.connect(addr); // TODO: implementar retentativas em caso de falhas ???
        clusterClientSocket.sendMore(commandEnum.cmd());
        clusterClientSocket.send(msg);

        System.out.printf("Command %s:%s sent to %s%n", commandEnum, msg, dest);
        String reply = clusterClientSocket.recvStr();
        System.out.printf("Received %s%n", reply);
        clusterClientSocket.disconnect(addr);
        return reply;
    }

    @Override
    protected void processMsg(List<String> msg) {
        System.out.printf("processing cluster msg: %s%n", msg);
        CommandEnum messageEnum = CommandEnum.getMessage(msg.get(2));
        StringBuilder reply;
        boolean needsUpdate = false;
        if (!CommandType.MEMBERSHIP.equals(messageEnum.cmdType())) {
            reply = new StringBuilder(String.format("message [%s] not recognized", msg.get(2)));
        } else {

            switch (messageEnum) {
                case CLUSTER_JOIN -> {
                    System.out.println(msg.get(3));
                    synchronized (this) {
                        getCluster().getNodes().putAll(new JSONObject(msg.get(3)).toMap());
                        getCluster().updateClusterHashNodes();
                    }
                    reply = new StringBuilder(REPLY_OK);
                    needsUpdate = true;
                }
                case CLUSTER_LEAVE -> {
                    synchronized (this) {
                        getCluster().getNodes().remove(new JSONObject(msg.get(3)).keys().next());
                        getCluster().updateClusterHashNodes();
                    }
                    reply = new StringBuilder(REPLY_OK);
                    needsUpdate = true;
                }
                case CLUSTER_UPDATE -> {
                    System.out.println(msg.get(3));
                    synchronized (this) {
                        getCluster().setNodes(new JSONObject(msg.get(3)).toMap());
                        getCluster().updateClusterHashNodes();
                    }
                    System.out.printf("Cluster updated : %s%n", getCluster().getNodes());
                    reply = new StringBuilder(REPLY_OK);
                }
                default -> reply = new StringBuilder(String.format("command [%s] not implemented", msg.get(2)));
            }
        }
        System.out.println(reply);
        getSocket().sendMore(msg.get(0));
        getSocket().sendMore("");
        getSocket().send(reply.toString());
        if (needsUpdate) onUpdateClusterNodes();
    }

    private void onUpdateClusterNodes() {
        System.out.printf("updating the cluster %s%n", getCluster().getNodes());
        getCluster().getNodes().values().forEach(ip -> {
            if (ip != getNode().getIp()) {
                sendMsg((String) ip, CommandEnum.CLUSTER_UPDATE, new JSONObject(getCluster().getNodes()).toString());
            }
        });
    }
}
