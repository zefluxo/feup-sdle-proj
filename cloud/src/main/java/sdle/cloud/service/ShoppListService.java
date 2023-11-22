package sdle.cloud.service;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;
import sdle.cloud.utils.ZMQUtils;

import java.util.List;

public class ShoppListService extends BaseService {

    // TODO: simplificacao da shopping list usada aqui apenas para avancar no mecanismo de particionamento do cluster
    private final ZMQ.Socket shoppListClientSocket;

    public ShoppListService(Node node, Cluster cluster) {
        super(node, cluster);
        shoppListClientSocket = ZMQUtils.newClientSocket(context);
    }


    @Override
    protected String getServicePort() {
        return getNode().getPort();
    }

    @Override
    protected void processMsg(List<String> msg) {
        System.out.printf("processing client msg: %s%n", msg);
        CommandEnum messageEnum = CommandEnum.getCommand(msg.get(1));
        messageEnum.getProcessor().process(getSocket(), shoppListClientSocket, msg, getCluster(), getNode());
    }
}
