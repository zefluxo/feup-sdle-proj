package sdle.cloud.service;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.message.CommandType;
import sdle.cloud.utils.HashUtils;

import java.util.*;

public class ShoppListService extends BaseService {

    public static final String REPLY_NOT_FOUND = "<not found>";
    public static final String REPLY_OK = "OK";
    // TODO: simplificacao da shopping list usada aqui apenas para avancar no mecanismo de particionamento do cluster
    private final Map<String, Map<String, Integer>> shoppLists = new HashMap<>();
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
            response = String.format("message [%s] not recognized", msg.get(2));
        } else {
            switch (messageEnum) {
                case PUT_LIST -> response = onPutListReceive(msg);
                case GET_LIST -> response = onGetListReceive(msg);
                case DELETE_LIST -> response = String.format("apagando a lista %s", msg.get(3));
                case PUT_ITEM -> response = onPutItemReceive(msg);
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

    private String onPutListReceive(List<String> msg) {
        String listHashId;
        if (msg.size() > 3) {
            listHashId = msg.get(3);
        } else {
            listHashId = HashUtils.getRandomHash();
        }
        String dest = "";
        for (String nodeHash : getCluster().getNodeHashes().keySet()) {
            System.out.printf(listHashId, nodeHash, listHashId.compareTo(nodeHash) > 0);
            if (listHashId.compareTo(nodeHash) > 0) {
                dest = getCluster().getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o ultimo node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        if (dest.isEmpty()) dest = getCluster().getNodeHashes().lastEntry().getValue();
        System.out.printf("%s, %s, %s%n", dest, getNode().getIp(), dest.equals(getNode().getIp()));
        if (dest.equals(getNode().getIp())) {
            shoppLists.put(listHashId, new HashMap<>());
            System.out.println(shoppLists);
        } else {
            sendMsg(dest, CommandEnum.PUT_LIST, Collections.singletonList(listHashId));
        }
        return listHashId;
    }

    private String onGetListReceive(List<String> msg) {
        String listHashId = msg.get(3);
        String dest = "";
        for (String nodeHash : getCluster().getNodeHashes().keySet()) {
            if (listHashId.compareTo(nodeHash) > 0) {
                dest = getCluster().getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o ultimo node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        if (dest.isEmpty()) dest = getCluster().getNodeHashes().lastEntry().getValue();
        System.out.printf("%s, %s, %s%n", dest, getNode().getIp(), dest.equals(getNode().getIp()));
        if (dest.equals(getNode().getIp())) {
            String reply = String.valueOf(shoppLists.get(listHashId));
            if ("null".equals(reply)) {
                reply = REPLY_NOT_FOUND;
            }
            return reply;
        } else {
            return sendMsg(dest, CommandEnum.GET_LIST, Collections.singletonList(listHashId));
        }
    }

    private String onPutItemReceive(List<String> msg) {
        String listHashId = msg.get(3);
        String dest = "";
        for (String nodeHash : getCluster().getNodeHashes().keySet()) {
            if (listHashId.compareTo(nodeHash) > 0) {
                dest = getCluster().getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o ultimo node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        if (dest.isEmpty()) dest = getCluster().getNodeHashes().lastEntry().getValue();
        System.out.printf("%s, %s, %s%n", dest, getNode().getIp(), dest.equals(getNode().getIp()));
        if (dest.equals(getNode().getIp())) {
            Map<String, Integer> shoppList = shoppLists.get(listHashId);
            if (shoppList == null) {
                return REPLY_NOT_FOUND;
            }
            shoppList.put(msg.get(4), Integer.valueOf(msg.get(5)));
            System.out.println(shoppLists);
        } else {
            sendMsg(dest, CommandEnum.PUT_ITEM, msg.subList(3, msg.size()));
        }
        return REPLY_OK;
    }

    private String sendMsg(String dest, CommandEnum commandEnum) {
        return sendMsg(dest, commandEnum, Collections.emptyList());
    }

    private String sendMsg(String dest, CommandEnum commandEnum, List<String> msg) {
        System.out.printf("Sending command %s:%s to %s%n", commandEnum, msg, dest);
        String addr = get0MQAddr(dest, getNode().getPort());
        shoppListClientSocket.connect(addr); // TODO: implementar retentativas em caso de falhas ???

        if (msg.isEmpty()) {
            shoppListClientSocket.send(commandEnum.cmd());
        } else {
            shoppListClientSocket.sendMore(commandEnum.cmd());
            msg.subList(0, msg.size() - 1).forEach(shoppListClientSocket::sendMore);
            shoppListClientSocket.send(msg.get(msg.size() - 1));
        }
        System.out.printf("Command %s:%s sent to %s%n", commandEnum, msg, dest);
        String reply = shoppListClientSocket.recvStr();
        System.out.printf("Received %s%n", reply);
        shoppListClientSocket.disconnect(addr);
        return reply;
    }

}
