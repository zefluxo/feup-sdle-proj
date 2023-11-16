package sdle.cloud.service;

import sdle.cloud.cluster.Node;
import sdle.cloud.message.MessageEnum;

import java.util.List;

public class ClusterService extends ServiceBase {
    public ClusterService(Node node) {
        super(node);
    }

    @Override
    protected String get0MQAddr() {

        return String.format("tcp://%s:%s", getNode().getHostname(), getNode().getClusterPort());
    }

    @Override
    protected void processMsg(List<String> msg) {
        System.out.printf("processing cluster msg: %s%n", msg);
        MessageEnum messageEnum = MessageEnum.getMessage(msg.get(2));
        String response;
        switch (messageEnum) {
            case CLUSTER_JOIN -> {
                response = String.format("adicionada nova lista %s", msg.get(3));
            }
            case CLUSTER_LEAVE -> {
                response = String.format("retornando a lista %s", msg.get(3));
            }
            case CLUSTER_JOIN_RESPONSE -> {
                response = String.format("apagando a lista %s", msg.get(3));
            }
            case CLUSTER_LEAVE_RESPONSE -> {
                response = String.format("adicionado item %s à lista %s", msg.get(4), msg.get(3));
            }
            default -> {
                response = String.format("command [%s] not implemented", msg.get(2));
            }
        }
        System.out.println(response);
        getSocket().sendMore(msg.get(0));
        getSocket().sendMore("");
        getSocket().send(response);
    }
}
