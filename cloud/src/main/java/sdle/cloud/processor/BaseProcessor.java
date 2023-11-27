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

    protected String getListOwner(Cluster cluster, String listHashId) {
        for (String nodeHash : cluster.getNodeHashes().keySet()) {
            //System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> : %s, %s, %s%n", listHashId, nodeHash, listHashId.compareTo(nodeHash) < 0);
            if (listHashId.compareTo(nodeHash) < 0) {
                return cluster.getNodeHashes().get(nodeHash);
            }
        }
        // se nao estiver setado deve se usar o primeiro node
        // (o listHashId da lista eh menor que todos os hashs dos nodes)
        return cluster.getNodeHashes().firstEntry().getValue();
    }

}
