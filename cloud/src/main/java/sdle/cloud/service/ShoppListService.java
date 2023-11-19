package sdle.cloud.service;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;

import java.util.List;
import java.util.Random;

public class ShoppListService extends BaseService {

    // TODO: simplificacao da shopping list usada aqui apenas para avancar no mecanismo de particionamento do cluster
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
        CommandEnum messageEnum = CommandEnum.getMessage(msg.get(2));
        messageEnum.getProcessor().process(getSocket(), shoppListClientSocket, msg, getCluster(), getNode());
    }
}
