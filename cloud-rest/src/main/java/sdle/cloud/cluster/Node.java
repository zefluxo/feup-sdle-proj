package sdle.cloud.cluster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;
import sdle.cloud.NodeConfiguration;
import sdle.cloud.utils.FileUtils;
import sdle.cloud.utils.HashUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Random;

@Data
@Singleton
public class Node {

    @Inject
    @JsonIgnore
    NodeConfiguration config;

    @Inject
    @JsonIgnore
    FileUtils fileUtils;

    String id;
    String hostname;
    String ip;
    Integer port;

    String hashId;

    boolean maintenance = false;
    boolean initializing = true;

    public Node() {
        //
    }

    @PostConstruct
    void onStart() throws UnknownHostException {
        hostname = Inet4Address.getLocalHost().getHostName();
        ip = Inet4Address.getLocalHost().getHostAddress();
        port = config.getNodePort();

        try {
            Node node = fileUtils.readNode();
            id = node.getId();
            hashId = node.getHashId();
        } catch (Exception ex) {
            id = config.getNodeId();
            if ("node".equals(id)) {
                id = "node" + new Random().nextInt();
            }
            hashId = HashUtils.getHash(id);
            fileUtils.writeNode(this);
        }
        System.out.printf("Initializing Node (%s)%n", this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        System.out.println("Node shutdown");
        fileUtils.writeNode(this);
    }
}
