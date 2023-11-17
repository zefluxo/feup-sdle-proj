package sdle.cloud.service;

import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.message.CommandType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClusterService extends BaseService {
    private final Map<String, Object> clusterNodes = new HashMap<String, Object>();
    private ZMQ.Socket clusterClientSocket;
    private int nextHostname = 0;

    public ClusterService(Node node) {
        super(node);
    }

    @Override
    public void init() {
        super.init();
        try (ZContext context = new ZContext()) {
            clusterClientSocket = context.createSocket(SocketType.REQ);
            clusterClientSocket.setIdentity((Thread.currentThread().getName() + new Random().nextInt(1000)).getBytes(ZMQ.CHARSET));

            clusterNodes.put(getNode().getId(), getNode().getIp());
            joinCluster();
        }
    }

    private void joinCluster() {
        JSONObject nodeJson = new JSONObject();
        nodeJson.put(getNode().getId(), getNode().getIp());
        String reply = sendMsg(CommandEnum.CLUSTER_JOIN, nodeJson.toString());
        System.out.printf("New cluster : %s", reply);
    }

    @Override
    protected String getServicePort() {
        return getNode().getClusterPort();
    }

    private String getNextBootstrapHostname() {
        nextHostname = (nextHostname + 1) % getNode().getBootstrapList().size();
        return getNode().getBootstrapList().get(nextHostname);
    }

    private String sendMsg(CommandEnum commandEnum, String msg) {
        String destHostname = getNextBootstrapHostname();
        System.out.printf("Sending command %s:%s to %s%n", commandEnum, msg, destHostname);
        clusterClientSocket.connect(get0MQAddr(destHostname, getNode().getClusterPort())); // TODO: implementar retentativas em caso de falhas
        clusterClientSocket.sendMore(commandEnum.cmd());
        clusterClientSocket.send(msg);

        System.out.printf("Command %s:%s sent to %s%n", commandEnum, msg, destHostname);
        String reply = clusterClientSocket.recvStr();
        System.out.printf("Received %s%n", reply);
        return reply;
    }

    @Override
    protected void processMsg(List<String> msg) {
        System.out.printf("processing cluster msg: %s%n", msg);
        CommandEnum messageEnum = CommandEnum.getMessage(msg.get(2));
        StringBuilder response;
        if (!CommandType.MEMBERSHIP.equals(messageEnum.cmdType())) {
            response = new StringBuilder(String.format("message [%s] not recognized", msg.get(2)));
        } else {

            switch (messageEnum) {
                case CLUSTER_JOIN -> {
//                    JSONObject argsJson = ;
                    System.out.println(msg.get(3));
                    clusterNodes.putAll(new JSONObject(msg.get(3)).toMap());
                    response = new StringBuilder(new JSONObject(clusterNodes).toString());
                }
                case CLUSTER_LEAVE -> {
                    response = new StringBuilder(String.format("retornando a lista %s", msg.get(3)));
                }
                case CLUSTER_JOIN_RESPONSE -> {
                    response = new StringBuilder(String.format("apagando a lista %s", msg.get(3)));
                }
                case CLUSTER_LEAVE_RESPONSE -> {
                    response = new StringBuilder(String.format("adicionado item %s à lista %s", msg.get(4), msg.get(3)));
                }
                default -> {
                    response = new StringBuilder(String.format("command [%s] not implemented", msg.get(2)));
                }
            }
        }
        System.out.println(response);
        getSocket().sendMore(msg.get(0));
        getSocket().sendMore("");
        getSocket().send(response.toString());
    }
}
