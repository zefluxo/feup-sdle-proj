package sdle.cloud.service;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.message.CommandType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShoppListService extends BaseService {

    // TODO: simplificacao da shopping list usada aqui apenas para avancar no mecanismo de particionamento do cluster
    private final Map<String, String> shoppLists = new HashMap<>();
    private final ZMQ.Socket shoppListClientSocket;

    public ShoppListService(Node node, Cluster cluster) {
        super(node, cluster);
        shoppListClientSocket = context.createSocket(SocketType.REQ);
        shoppListClientSocket.setIdentity((Thread.currentThread().getName() + new Random().nextInt(1000)).getBytes(ZMQ.CHARSET));

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
                    int hash = msg.get(3).hashCode();
                    String dest = "";
                    for (Integer nodeHash : getCluster().getNodeHashes().keySet()) {
                        if (hash > nodeHash) {
                            dest = getCluster().getNodeHashes().get(nodeHash);
                        }
                    }
                    // se nao estiver setado deve se usar o ultimo node
                    // (o hash da lista eh menor que todos os hashs dos nodes)
                    if (dest.isEmpty()) dest = getCluster().getNodeHashes().lastEntry().getValue();
                    System.out.printf("%s, %s, %s%n", dest, getNode().getIp(), dest.equals(getNode().getIp()));
                    if (dest.equals(getNode().getIp())) {
                        shoppLists.put(String.valueOf(hash), msg.get(3));
                        System.out.println(shoppLists);
                    } else {
                        sendMsg(dest, CommandEnum.PUT_LIST, msg.get(3));
                    }
                    response = "OK";
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

    private String sendMsg(String dest, CommandEnum commandEnum, String msg) {
        System.out.printf("Sending command %s:%s to %s%n", commandEnum, msg, dest);
        String addr = get0MQAddr(dest, getNode().getPort());
        shoppListClientSocket.connect(addr); // TODO: implementar retentativas em caso de falhas ???
        shoppListClientSocket.sendMore(commandEnum.cmd());
        shoppListClientSocket.send(msg);

        System.out.printf("Command %s:%s sent to %s%n", commandEnum, msg, dest);
        String reply = shoppListClientSocket.recvStr();
        System.out.printf("Received %s%n", reply);
        shoppListClientSocket.disconnect(addr);
        return reply;
    }

}
