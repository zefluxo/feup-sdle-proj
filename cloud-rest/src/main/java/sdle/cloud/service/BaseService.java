package sdle.cloud.service;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.HashUtils;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public abstract class BaseService {
    public static final int REPLICATE_FACTOR = 2;
    public static final String REPLY_OK = "OK";

    WebClient restClient;

    protected void init(Cluster cluster, Node node) {

        cluster.getNodes().put(node.getId(), node.getIp());
        cluster.updateClusterHashNodes();

        restClient = WebClient.create(Vertx.vertx(),
                new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1).setDefaultPort(node.getPort())
        );
        //
    }

    protected String getNextBootstrapHostAddr(Cluster cluster, Node node) {
        //System.out.println(config.getBootstrapList());
        String ip = node.getIp();
        while (Objects.equals(ip, node.getIp())) {
            List<String> bootstrapList = node.getConfig().getBootstrapList();
            ip = bootstrapList.get(cluster.getNextBootstrapHost());
            cluster.setNextBootstrapHost((cluster.getNextBootstrapHost() + 1) % bootstrapList.size());
        }
        return ip;
    }

    protected String getListOwner(Cluster cluster, Node node, String listHashId) {
        TreeMap<String, String> nodeHashes = cluster.getNodeHashes();
        if (nodeHashes.isEmpty()) return getNextBootstrapHostAddr(cluster, node);
        String nextHashId = HashUtils.getNextHashId(listHashId, nodeHashes);
        return nodeHashes.get(nextHashId);
    }
}
