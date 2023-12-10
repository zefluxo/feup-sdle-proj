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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class ClusterService extends BaseService {
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
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
        cluster.writeListsOnDisk();
        //System.out.printf(" heartbeat failures: %s%n", heartbeatFailures);
    }

    private void joinCluster() {
        restClient.post(getNextBootstrapHostAddr(cluster, node), String.format("/api/cluster/joining/%s/%s",
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
            cluster.getNodes().remove(id);
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
            String ownerIp = getListOwner(cluster, node, shoppListHashId);
            try {
                restClient.post(ownerIp, String.format("/api/shopp/list/%s", shoppListHashId))
                        .sendJson(shoppList).toCompletionStage().toCompletableFuture().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        cluster.getReplicateShoppLists().forEach((shoppListHashId, shoppList) -> {
            String ownerIp = getListOwner(cluster, node, shoppListHashId);
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

        List<String> newNodeIds = newClusterNodes.keySet().stream().filter(key -> !cluster.getNodes().containsKey(key))
                .toList();
        synchronized (this) {
            cluster.getNodes().clear();
            cluster.getNodes().putAll(newClusterNodes);
            cluster.updateClusterHashNodes();
        }
        if (node.isInitializing() || cluster.getNodeHashes().isEmpty()) {
            node.setInitializing(false);
        } else {
            List<String> toBeRemoved = new ArrayList<>();
            AtomicReference<String> nextHashId = new AtomicReference<>();
            newNodeIds.forEach(newNodeId -> {
                nextHashId.set(HashUtils.getNextHashId(HashUtils.getHash(newNodeId), cluster.getNodeHashes()));
                if (node.getHashId().equals(nextHashId.get())) {
                    cluster.getShoppLists().forEach((shoppListHashId, shoppList) -> {
                        String ownerIp = getListOwner(cluster, node, shoppListHashId);
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
        }
        return REPLY_OK;
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
