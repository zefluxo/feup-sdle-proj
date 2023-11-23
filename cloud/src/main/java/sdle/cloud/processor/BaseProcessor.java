package sdle.cloud.processor;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.ZMQAdapter;

import java.util.List;

public abstract class BaseProcessor {
    public static final String REPLY_NOT_FOUND = "<not found>";
    public static final String REPLY_OK = "OK";

    abstract public String process(ZMQ.Socket serverSocket, ZMQAdapter zmqAdapter, List<String> msg, Cluster cluster, Node node);

}
