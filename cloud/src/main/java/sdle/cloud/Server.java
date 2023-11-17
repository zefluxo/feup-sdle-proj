package sdle.cloud;

import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.service.ClusterService;
import sdle.cloud.service.ShoppListService;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final String DEFAULT_SHOPP_LIST_PORT = "7777";
    public static final String DEFAULT_CLUSTER_PORT = "7787";

    public static final String DEFAULT_BOOTSTRAP = "localhost";
    //    public static final String DEFAULT_BOOTSTRAP = "10.5.0.11,10.5.0.12,10.5.0.13";
    private static final ExecutorService servicesExecutor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws UnknownHostException {
        Node node = new Node(System.getenv().getOrDefault("SDLE_NODE_ID", "node" + new Random().nextInt()),
                Inet4Address.getLocalHost().getHostName(),
                Inet4Address.getLocalHost().getHostAddress(),
                System.getenv().getOrDefault("SDLE_NODE_SHOPP_LIST_PORT", DEFAULT_SHOPP_LIST_PORT),
                System.getenv().getOrDefault("SDLE_NODE_CLUSTER_PORT", DEFAULT_CLUSTER_PORT),
                System.getenv().getOrDefault("SDLE_NODE_BOOTSTRAP", DEFAULT_BOOTSTRAP));
        System.out.printf("Initializing Node (%s)%n", node);
        Cluster cluster = new Cluster();
        ClusterService clusterService = new ClusterService(node, cluster);
        ShoppListService shoppListService = new ShoppListService(node, cluster);
        servicesExecutor.submit(clusterService::init);
        servicesExecutor.submit(shoppListService::init);
    }
}