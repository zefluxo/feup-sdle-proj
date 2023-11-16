package sdle.cloud;

import sdle.cloud.cluster.Node;
import sdle.cloud.service.ClusterService;
import sdle.cloud.service.ShoppListService;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final String DEFAULT_SHPP_LIST_PORT = "7777";
    public static final String DEFAULT_CLUSTER_PORT = "7778";
    public static final String DEFAULT_BOOTSTRAP = "10.5.0.11,10.5.0.12,10.5.0.13";
    private static final Properties properties = System.getProperties();
    private static final ExecutorService servicesExecutor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws UnknownHostException {

        //System.out.println(properties.getOrDefault("sdle.node.id", new ArrayList<>()));
        Node node = new Node(properties.getProperty("sdle.node.id", "node" + new Random().nextInt()),
                Inet4Address.getLocalHost().getHostName(),
                Inet4Address.getLocalHost().getHostAddress(),
                properties.getProperty("sdle.node.port", DEFAULT_SHPP_LIST_PORT),
                properties.getProperty("sdle.node.clusterPort", DEFAULT_CLUSTER_PORT),
                properties.getProperty("sdle.node.bootstrap", DEFAULT_BOOTSTRAP));
        System.out.printf("Initializing Node (%s)%n", node);
        ClusterService clusterService = new ClusterService(node);
        ShoppListService shoppListService = new ShoppListService(node);
        servicesExecutor.submit(clusterService::init);
        servicesExecutor.submit(shoppListService::init);
    }
}