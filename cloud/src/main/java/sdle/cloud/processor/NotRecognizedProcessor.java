package sdle.cloud.processor;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;

import java.util.List;

public class NotRecognizedProcessor extends BaseProcessor {

    @Override
    public String process(ZMQ.Socket sertverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node) {
        return "msg not recognized";
    }
}
