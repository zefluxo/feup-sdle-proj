package sdle.cloud.service;

import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.message.CommandType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppListService extends BaseService {

    // TODO: simplificacao da shopping list usada aqui apenas para avancar no mecanismo de particionamento do cluster
    private final Map<String, String> shoppLists = new HashMap<>();

    public ShoppListService(Node node, Cluster cluster) {
        super(node, cluster);
    }

    @Override
    protected String getServicePort() {
        return getNode().getPort();
    }

    @Override
    protected void processMsg(List<String> msg) {
        System.out.printf("processing client msg: %s%n", msg);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CommandEnum messageEnum = CommandEnum.getMessage(msg.get(2));
        String response;
        if (!CommandType.SHOPP_LIST.equals(messageEnum.cmdType())) {
            response = String.format("message [%s] not recongnized", msg.get(2));
        } else {
            switch (messageEnum) {
                case PUT_LIST -> {
                    String hash = String.valueOf(msg.get(3).hashCode());

                    response = String.format("adicionada nova lista %s", msg.get(3));
                }
                case GET_LIST -> response = String.format("retornando a lista %s", msg.get(3));
                case DELETE_LIST -> response = String.format("apagando a lista %s", msg.get(3));
                case PUT_ITEM -> response = String.format("adicionado item %s à lista %s", msg.get(4), msg.get(3));
                case GET_ITEM -> response = String.format("retornando item %s da lista %s", msg.get(4), msg.get(3));
                case DELETE_ITEM -> response = String.format("apagando o item %s da lista %s", msg.get(4), msg.get(3));
                default -> response = String.format("message [%s] not found", msg.get(2));
            }
        }
        System.out.println(response);
        getSocket().sendMore(msg.get(0));
        getSocket().sendMore("");
        getSocket().send(response);
    }
}
