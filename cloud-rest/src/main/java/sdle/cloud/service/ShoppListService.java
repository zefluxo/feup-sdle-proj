package sdle.cloud.service;

import io.quarkus.runtime.StartupEvent;
import io.vertx.codegen.annotations.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;

import java.net.UnknownHostException;
import java.util.*;

@ApplicationScoped
public class ShoppListService extends BaseService {

    @Inject
    Node node;

    @Inject
    Cluster cluster;

    ShoppListService() {
        //
    }

    void onStart(@Observes StartupEvent ev) throws UnknownHostException {
        init(cluster, node);
    }


    @SneakyThrows
    public String processPutList(String listHashId, Map<String, Integer> shoppList) {
        System.out.printf("PUT LIST process %s, %s %n", listHashId, shoppList);
        String ownerIp = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s, %s %n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()), shoppList);
        if (ownerIp.equals(node.getIp())) {
            cluster.getShoppLists().put(listHashId, shoppList);
            cluster.getReplicateShoppLists().remove(listHashId);

            sendReplicateList(listHashId, shoppList);
            System.out.println(cluster.getShoppLists());
        } else {
            if (shoppList.isEmpty()) {
                String url = String.format("/api/shopp/list/%s", listHashId);
                return restClient.put(ownerIp, url).send()
                        .toCompletionStage().toCompletableFuture().get().bodyAsString();
            } else {
                String url = String.format("/api/shopp/list/%s", listHashId);
                return restClient.post(ownerIp, url).sendJson(shoppList)
                        .toCompletionStage().toCompletableFuture().get().bodyAsString();
            }
        }
        System.out.println("Returning :" + listHashId);
        return listHashId;
    }


    @SneakyThrows
    public Map<String, Integer> processGetList(String listHashId) {
        System.out.printf("GET LIST process %s%n", listHashId);
        String ownerIp = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()));
        @Nullable Map<String, Integer> reply;
        if (ownerIp.equals(node.getIp())) {
            reply = cluster.getShoppLists().get(listHashId);
        } else {
            if (cluster.getReplicateShoppLists().containsKey(listHashId)) {
                reply = cluster.getReplicateShoppLists().get(listHashId);
            } else {
                String url = String.format("/api/shopp/list/%s", listHashId);
                reply = restClient.get(ownerIp, url)
                        .send().toCompletionStage().toCompletableFuture().get().bodyAsJson(Map.class);
            }
        }
        return reply;

    }

    @SneakyThrows
    public String processPutItem(String listHashId, String name, Integer quantity) {
        System.out.printf("PUT ITEM process %s %s %s%n", listHashId, name, quantity);
        String ownerIp = getListOwner(cluster, listHashId);
        System.out.printf("%s, %s, %s%n", ownerIp, node.getIp(), ownerIp.equals(node.getIp()));
        String reply;
        if (ownerIp.equals(node.getIp())) {
            Map<String, Integer> shoppList = cluster.getShoppLists().get(listHashId);
            if (shoppList == null) {
                reply = null;
            } else {
                shoppList.put(name, quantity);
                sendReplicateList(listHashId, shoppList);
                System.out.println(cluster.getShoppLists());
                reply = REPLY_OK;
            }
        } else {
            String url = String.format("/api/shopp/list/%s/%s/%s", listHashId, name, quantity);
            reply = restClient.post(ownerIp, url)
                    .send().toCompletionStage().toCompletableFuture().get().bodyAsString();
        }
        return reply;
    }

    protected void sendReplicateList(String listHashId, Map<String, Integer> shoppList) {
        List<String> replicaHashes = getReplicateHashes();
        System.out.println(listHashId + " Will be replicate to " + replicaHashes);
        replicaHashes.forEach(hash -> {
            restClient.post(cluster.getNodeHashes().get(hash), String.format("/api/shopp/replicate/%s", listHashId))
                    .sendJson(shoppList)
                    .onFailure(Throwable::printStackTrace)
                    .onSuccess(r -> System.out.println(listHashId + " replicated to " + cluster.getNodeHashes().get(hash)));
        });
    }

    protected List<String> getReplicateHashes() {
        Set<String> hashes = cluster.getNodeHashes().keySet();
        System.out.println(hashes);
        Iterator<String> iterator = hashes.iterator();
        String next = "";
        List<String> replicateHashes = new ArrayList<>();
        while (iterator.hasNext()) {
            next = iterator.next();
            System.out.println(next);
            System.out.println(node.getHashId());
            System.out.println(next.equals(node.getHashId()));
            if (next.equals(node.getHashId())) {
                for (int i = 1; i < (Math.min(REPLICATE_FACTOR, cluster.getNodeHashes().size())); i++) {
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
    public String processReplicateList(String listHashId, Map<String, Integer> shoppList) {
        System.out.printf("REPLICATE LIST process %s %s%n", listHashId, shoppList);
        if (node.isMaintenance()) {
            String url = String.format("/api/shopp/replicate/%s", listHashId);
            return restClient.post((String) cluster.getNodes().values().stream().findFirst().get(), url)
                    .sendJson(shoppList).toCompletionStage().toCompletableFuture().get().bodyAsString();
        } else {
            cluster.getReplicateShoppLists().put(listHashId, shoppList);
        }
        return listHashId;
    }
}
