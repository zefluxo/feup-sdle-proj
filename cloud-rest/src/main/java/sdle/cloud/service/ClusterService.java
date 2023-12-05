package sdle.cloud.service;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import sdle.cloud.NodeConfiguration;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.HashUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@ApplicationScoped
public class ClusterService extends BaseService {
    private static final Integer MAX_HEARTBEAT_FAILURES = 2;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
    private final Map<String, Integer> heartbeatFailures = new ConcurrentHashMap<>();


    @Inject
    NodeConfiguration config;
    @Inject
    Node node;

    @Inject
    Cluster cluster;

    ClusterService() {
        //
    }

    void onStart(@Observes StartupEvent ev) {
        init(cluster, node);
        scheduledExecutor.schedule(this::joinCluster, 2, TimeUnit.SECONDS);
        scheduledExecutor.scheduleWithFixedDelay(this::heartBeat, 5, 15, TimeUnit.SECONDS);
    }


    private void heartBeat() {
        cluster.printStatus(node);
        //System.out.printf(" heartbeat failures: %s%n", heartbeatFailures);
    }

    private void joinCluster() {
        restClient.post(getNextBootstrapHostAddr(), String.format("/api/cluster/joining/%s/%s",
                        node.getId(),
                        node.getIp()))
                .send()
                .onFailure(Throwable::printStackTrace)
                .onSuccess(r -> System.out.println(r.statusCode() + ": Join sent to  cluster"));
    }

    void onStop(@Observes ShutdownEvent ev) {
        System.out.println("Terminating and leaving cluster");
        if (!node.isMaintenance()) {
            cluster.getNodes().remove(node.getId());
            sendLeavingMsg();
        }
    }

    private void sendLeavingMsg() {
        cluster.getNodes().values().forEach(ip -> {
            restClient.post((String) ip, String.format("/api/cluster/leaving/%s",
                            node.getId()))
                    .send()
                    .onFailure(Throwable::printStackTrace)
                    .onSuccess(r -> System.out.printf("Leaving cluster sended to %s", ip));
        });
    }

    private String getNextBootstrapHostAddr() {
        //System.out.println(config.getBootstrapList());
        String ip = node.getIp();
        while (Objects.equals(ip, node.getIp())) {
            ip = config.getBootstrapList().get(cluster.getNextBootstrapHost());
            cluster.setNextBootstrapHost((cluster.getNextBootstrapHost() + 1) % config.getBootstrapList().size());
        }
        return ip;
    }

    public String processJoining(String id, String ip) {
        System.out.printf("JOINING process %s, %s%n", id, ip);
        synchronized (this) {
            cluster.getNodes().put(id, ip);
            cluster.updateClusterHashNodes();
        }
        notifyClusterNodesUpdate();

        return REPLY_OK;
    }

    public String processLeaving(String id) {
        System.out.printf("LEAVING process %s%n", id);
        synchronized (this) {
            System.out.println(cluster.getNodes());
            cluster.getNodes().remove(id);
            System.out.println(cluster.getNodes());
            cluster.updateClusterHashNodes();
        }
        notifyClusterNodesUpdate();
        return REPLY_OK;
    }

    @SneakyThrows
    public String processLeave() {
        System.out.printf("LEAVE process %n");

        node.setMaintenance(true);

        cluster.getNodes().remove(node.getId());
        cluster.getNodeHashes().remove(node.getHashId());

        sendLeavingMsg();

        cluster.getShoppLists().forEach((shoppListHashId, shoppList) -> {
            String ownerIp = getListOwner(cluster, shoppListHashId);
            try {
                restClient.post(ownerIp, String.format("/api/shopp/list/%s", shoppListHashId))
                        .sendJson(shoppList).toCompletionStage().toCompletableFuture().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        cluster.getReplicateShoppLists().forEach((shoppListHashId, shoppList) -> {
            String ownerIp = getListOwner(cluster, shoppListHashId);
            try {
                restClient.post(ownerIp, String.format("/api/shopp/list/%s", shoppListHashId))
                        .sendJson(shoppList).toCompletionStage().toCompletableFuture().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);

        return REPLY_OK;
    }

    public String processUpdate(Map<String, String> newClusterNodes) {
        System.out.printf("UPDATE process %s%n", newClusterNodes);

        if (newClusterNodes.equals(cluster.getNodes())) return REPLY_OK;

        Stream<Map.Entry<String, String>> newNodes = newClusterNodes.entrySet().stream().filter(ip -> !cluster.getNodes().containsKey(ip));
        synchronized (this) {
            cluster.getNodes().clear();
            cluster.getNodes().putAll(newClusterNodes);
            cluster.updateClusterHashNodes();
        }
        newNodes.forEach(newNode -> {
            List<String> toBeRemoved = new ArrayList<>();
            String nextHashId = getNextHashId(HashUtils.getHash(newNode.getKey()));
            if (node.getHashId().equals(nextHashId)) {
                cluster.getShoppLists().forEach((shoppListHashId, shoppList) -> {
                    String ownerIp = getListOwner(cluster, shoppListHashId);
                    if (!ownerIp.equals(node.getIp())) {
                        try {
                            restClient.post(ownerIp, String.format("/api/shopp/list/%s", shoppListHashId))
                                    .sendJson(shoppList).toCompletionStage().toCompletableFuture().get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        toBeRemoved.add(shoppListHashId);
                    }
                });
                toBeRemoved.forEach(shoppListHashId -> cluster.getShoppLists().remove(shoppListHashId));
            }
            //System.out.printf("%s%n %s%n %s %s%n", cluster.getNodeHashes(), toBeRemoved, node.getHashId(), nextHashId);
        });
        return REPLY_OK;
    }

    private String getNextHashId(String newHashId) {
        if (cluster.getNodeHashes().containsKey(newHashId)) {
            Map.Entry<String, String> nextNodeEntry = cluster.getNodeHashes().higherEntry(newHashId);
            if (nextNodeEntry != null) {
                return nextNodeEntry.getKey();
            }
            return cluster.getNodeHashes().firstEntry().getKey();
        }
        return null;
    }

    public void notifyClusterNodesUpdate() {
        System.out.printf("Updating the cluster %s%n", cluster.getNodes());
        cluster.getNodes().values().forEach(ip -> {
            if (ip != node.getIp()) {
                restClient.post((String) ip, "/api/cluster/update")
                        .sendJson(cluster.getNodes())
                        .onFailure(Throwable::printStackTrace)
                        .onSuccess(r -> System.out.println(r.statusCode() + ": Cluster update notify with success"));
            }
        });
    }


}
