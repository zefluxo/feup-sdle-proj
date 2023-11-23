package sdle.cloud.processor;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.ZMQAdapter;

import java.util.List;

public class NotRecognizedProcessor extends BaseProcessor {
    @Override
    public String process(ZMQ.Socket sertverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node) {
        return "msg not recognized";
    }
}
