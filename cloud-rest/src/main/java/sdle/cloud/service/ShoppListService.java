package sdle.cloud.service;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.utils.FileUtils;
import sdle.crdt.implementations.ORMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class ShoppListService extends BaseService {

    @Inject
    Node node;

    @Inject
    Cluster cluster;
    @Inject
    FileUtils fileUtils;

    ShoppListService() {
        //
    }

    void onStart(@Observes StartupEvent ev) {
        init(cluster, node);
    }


    @SneakyThrows
    public String processPutList(String hashId, ORMap shoppList) {
        System.out.printf("PUT LIST process %s, %s %s%n", hashId, shoppList, shoppList.getKernel());
        String ownerIp = getListOwner(cluster, node, hashId);
        //System.out.printf("%s, %s, %s, %s %n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()), shoppList);
        if (ownerIp.equals(node.getIp())) {
            cluster.getShoppLists().putIfAbsent(hashId, new ORMap());
            ORMap cloudShoppList = cluster.getShoppLists().get(hashId);
            cloudShoppList.join(shoppList);
            cluster.getShoppLists().put(hashId, new ORMap(cloudShoppList));
            cluster.getReplicateShoppLists().remove(hashId);
            sendReplicateList(hashId, cluster.getShoppLists().get(hashId));
        } else {
            String url = String.format("/api/shopp/list/%s", hashId);
            if (shoppList.getMap().isEmpty()) {
                return restClient.put(ownerIp, url).send()
                        .toCompletionStage().toCompletableFuture().get().bodyAsString();
            } else {
                return restClient.post(ownerIp, url).sendJson(shoppList)
                        .toCompletionStage().toCompletableFuture().get().bodyAsString();
            }
        }
        System.out.printf("Returning : %s%n", hashId);
        return hashId;
    }


    @SneakyThrows
    public ORMap processGetList(String listHashId) {
        System.out.printf("GET LIST process %s%n", listHashId);
        String ownerIp = getListOwner(cluster, node, listHashId);
//        System.out.printf("%s, %s, %s%n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()));
        ORMap reply;
        if (ownerIp.equals(node.getIp())) {
            reply = cluster.getShoppLists().get(listHashId);
        } else {
            if (cluster.getReplicateShoppLists().containsKey(listHashId)) {
                reply = cluster.getReplicateShoppLists().get(listHashId);
            } else {
                String url = String.format("/api/shopp/list/%s", listHashId);
                reply = restClient.get(ownerIp, url)
                        .send().toCompletionStage().toCompletableFuture().get().bodyAsJson(ORMap.class);
            }
        }
        System.out.printf("Returning : %s%n", reply);
        return reply;

    }

    @SneakyThrows
    public String processDeleteList(String listHashId) {
        System.out.printf("DELETE LIST process %s%n", listHashId);
        String ownerIp = getListOwner(cluster, node, listHashId);
//        System.out.printf("%s, %s, %s%n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()));
        String reply = REPLY_OK;
        if (ownerIp.equals(node.getIp())) {
            cluster.getShoppLists().remove(listHashId);
            fileUtils.deleteShoppList(listHashId);
            List<String> replicaHashes = getReplicateHashes();
            final List<Future<HttpResponse<Buffer>>> futures = new ArrayList<>();
            replicaHashes.forEach(hash -> futures.add(restClient.delete(cluster.getNodeHashes().get(hash),
                    String.format("/api/shopp/replicate/%s", listHashId)).send()));
            futures.forEach(f -> {
                try {
                    f.toCompletionStage().toCompletableFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error on delete : " + e.getMessage());
                }
            });
        } else {
            String url = String.format("/api/shopp/list/%s", listHashId);
            reply = restClient.delete(ownerIp, url)
                    .send().toCompletionStage().toCompletableFuture().get().bodyAsString();

        }
        System.out.printf("Returning : %s%n", reply);
        return reply;
    }

    @SneakyThrows
    public String processDeleteReplicatedList(String listHashId) {
        System.out.printf("DELETE REPLICATED LIST process %s%n", listHashId);
        cluster.getReplicateShoppLists().remove(listHashId);
        fileUtils.deleteReplicateShoppList(listHashId);
        return REPLY_OK;
    }

    @SneakyThrows
    public String processPutItem(String listHashId, String name, Integer quantity, boolean isInc) {
        String incOrDec = isInc ? "inc" : "dec";
        System.out.printf("CHANGE ITEM process %s (%s) %s %s  %n", listHashId, name, quantity, incOrDec);
        String ownerIp = getListOwner(cluster, node, listHashId);
        //System.out.printf("%s, %s, %s%n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()));
        String reply;
        if (ownerIp.equals(node.getIp())) {
            ORMap shoppList = cluster.getShoppLists().get(listHashId);
            if (shoppList == null) {
                reply = null;
            } else {
                if (isInc) {
                    shoppList.inc(name, quantity);
                } else {
                    shoppList.dec(name, quantity);
                }
                sendReplicateList(listHashId, shoppList);
                //System.out.println(cluster.getShoppLists());
                reply = REPLY_OK;
            }
        } else {
            String url = String.format("/api/shopp/list/%s/%s/%s/%s", listHashId, incOrDec, name, quantity);
            reply = restClient.post(ownerIp, url)
                    .send().toCompletionStage().toCompletableFuture().get().bodyAsString();
        }
        return reply;
    }

    protected void sendReplicateList(String listHashId, ORMap shoppList) {
        List<String> replicaHashes = getReplicateHashes();
        System.out.println(listHashId + " Will be replicate to " + replicaHashes);
        final List<Future<HttpResponse<Buffer>>> futures = new ArrayList<>();
        replicaHashes.forEach(hash -> futures.add(restClient.post(cluster.getNodeHashes().get(hash),
                        String.format("/api/shopp/replicate/%s", listHashId))
                .sendJson(shoppList)));
        futures.forEach(f -> {
            try {
                f.toCompletionStage().toCompletableFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Error on replication : " + e.getMessage());
            }
        });
    }

    protected List<String> getReplicateHashes() {
        Set<String> hashes = cluster.getNodeHashes().keySet();
        System.out.println(hashes);
        Iterator<String> iterator = hashes.iterator();
        String next;
        List<String> replicateHashes = new ArrayList<>();
        while (iterator.hasNext()) {
            next = iterator.next();
            if (next.equals(node.getHashId())) {
                for (int i = 1; i < (Math.min(node.getConfig().getReplicationFactor(), cluster.getNodeHashes().size())); i++) {
                    if (!iterator.hasNext()) {
                        iterator = hashes.iterator();
                    }
                    replicateHashes.add(iterator.next());
                }
                break;
            }
        }
        return replicateHashes;
    }

    @SneakyThrows
    public String processReplicateList(String hashId, ORMap shoppList) {
        System.out.printf("REPLICATE LIST process %s: %s%n", hashId, shoppList.getMap());
        if (node.isMaintenance()) {
            String url = String.format("/api/shopp/replicate/%s", hashId);
            return restClient.post((String) cluster.getNodes().values().stream().findFirst().get(), url)
                    .sendJson(shoppList).toCompletionStage().toCompletableFuture().get().bodyAsString();
        } else {
            cluster.getReplicateShoppLists().put(hashId, shoppList);
        }
        return hashId;
    }

}
