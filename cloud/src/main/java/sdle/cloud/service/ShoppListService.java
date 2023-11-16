package sdle.cloud.service;

import sdle.cloud.cluster.Node;
import sdle.cloud.message.MessageEnum;
import sdle.cloud.message.MessageType;

import java.util.List;

public class ShoppListService extends ServiceBase {
    public ShoppListService(Node node) {
        super(node);
    }

    @Override
    protected String get0MQAddr() {
        return String.format("tcp://*:%s", getNode().getPort());
    }

    @Override
    protected void processMsg(List<String> msg) {
        System.out.printf("processing client msg: %s%n", msg);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        MessageEnum messageEnum = MessageEnum.getMessage(msg.get(2));
        String response;
        if (!MessageType.SHOPP_LIST.equals(messageEnum.msgType())) {
            response = String.format("message [%s] not recongnized", msg.get(2));
        } else {
            switch (messageEnum) {
                case PUT_LIST -> {
                    response = String.format("adicionada nova lista %s", msg.get(3));
                }
                case GET_LIST -> {
                    response = String.format("retornando a lista %s", msg.get(3));
                }
                case DELETE_LIST -> {
                    response = String.format("apagando a lista %s", msg.get(3));
                }
                case PUT_ITEM -> {
                    response = String.format("adicionado item %s à lista %s", msg.get(4), msg.get(3));
                }
                case GET_ITEM -> {
                    response = String.format("retornando item %s da lista %s", msg.get(4), msg.get(3));
                }
                case DELETE_ITEM -> {
                    response = String.format("apagando o item %s da lista %s", msg.get(4), msg.get(3));
                }
                default -> {
                    response = String.format("message [%s] not found", msg.get(2));
                }
            }
        }
        System.out.println(response);
        getSocket().sendMore(msg.get(0));
        getSocket().sendMore("");
        getSocket().send(response);
    }
}
