package sdle.cloud.service;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseService {
    public static final int REPLICATE_FACTOR = 3;
    public static final String REPLY_OK = "OK";
    private static final ExecutorService executor = Executors.newFixedThreadPool(50);

    WebClient restClient;

    protected void init(Cluster cluster, Node node) {
        System.out.println(cluster);
        System.out.println(node);

        cluster.getNodes().put(node.getId(), node.getIp());
        cluster.updateClusterHashNodes();

        restClient = WebClient.create(Vertx.vertx(),
                new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1).setDefaultPort(node.getPort())
        );
        //
    }


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
