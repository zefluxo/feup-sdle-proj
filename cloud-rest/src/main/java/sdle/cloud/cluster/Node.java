package sdle.cloud.cluster;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;
import sdle.cloud.NodeConfiguration;
import sdle.cloud.utils.HashUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Random;

@Data
@Singleton
public class Node {

    @Inject
    NodeConfiguration config;

    String id;
    String hostname;
    String ip;
    Integer port;

    String hashId;

    boolean maintenance = false;
    boolean initializing = true;

    Node() {
        //
    }

    @PostConstruct
    void onStart() throws UnknownHostException {
        hostname = Inet4Address.getLocalHost().getHostName();
        ip = Inet4Address.getLocalHost().getHostAddress();
        port = config.getNodePort();
        id = config.getNodeId();
        if ("node".equals(id)) {
            id = "node" + new Random().nextInt();
        }
        hashId = HashUtils.getHash(id);
        System.out.printf("Initializing Node (%s)%n", this);
    }

}
