package sdle.cloud.service;

import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;

import java.util.List;

public class ShoppListService extends BaseService {

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
        CommandEnum messageEnum = CommandEnum.getCommand(msg.get(1));
        messageEnum.getProcessor().process(getSocket(), zmqAdapter, msg, getCluster(), getNode());
    }
}
