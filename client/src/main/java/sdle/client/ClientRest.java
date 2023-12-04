package sdle.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientRest {
    public static void main(String[] args) throws InterruptedException {
        Arrays.stream(args).forEach(System.out::println);
        ExecutorService executor = Executors.newCachedThreadPool();
        //  Socket to talk to server
        if (args.length < 1) {
            sendPredefined(executor);
//        } else {
//            List<String> msgArgs = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
//            sendRequest(args[0], args[1], msgArgs, 0);
        }


    }

    private static void sendPredefined(ExecutorService executor) throws InterruptedException {
        System.out.println("Connecting to cloud server (rest)");
        WebClient restClient = WebClient.create(Vertx.vertx(),
                new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2).setDefaultPort(7788));


        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
            int finalRequestNbr = requestNbr;
            executor.submit(() -> sendRequest(restClient, "host.docker.internal", "putList", Collections.emptyList(), finalRequestNbr));
        }
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    @SneakyThrows
    private static void sendRequest(WebClient restClient, String url, String cmd, List<String> msgArgs, int requestNbr) {
        HttpResponse<Buffer> response = restClient.put(url, "/api/shopp/list")
                .send().toCompletionStage().toCompletableFuture().get();
        System.out.printf("%s : status=%s : response: %s%n", requestNbr, response.statusCode(), response.bodyAsString());
    }
}
