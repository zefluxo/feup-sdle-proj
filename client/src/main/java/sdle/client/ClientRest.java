package sdle.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientRest {

    public static final String SERVER_ADDR = System.getenv().getOrDefault("sdle.client.serverAddr", "localhost");

    public static void main(String[] args) throws InterruptedException {
        Arrays.stream(args).forEach(System.out::println);
        ExecutorService executor = Executors.newCachedThreadPool();
        //  Socket to talk to server

        WebClient restClient = WebClient.create(Vertx.vertx(),
                new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1).setDefaultPort(7788).setConnectTimeout(2000));


        if (args.length < 1) {
            parallelPutListTest(executor, restClient);
//        } else {
//            List<String> msgArgs = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
//            sendRequest(args[0], args[1], msgArgs, 0);
        }

        executor.awaitTermination(10, TimeUnit.SECONDS);

    }

    @SneakyThrows

    private static void parallelPutListTest(ExecutorService executor, WebClient restClient) throws InterruptedException {
        System.out.println("Initializing parallel test");
        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
            int finalRequestNbr = requestNbr;
            executor.submit(() -> sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null, finalRequestNbr));
        }
    }

    @SneakyThrows
    public static HttpResponse<Buffer> sendSync(WebClient restClient, String serverAddr, HttpMethod method, String url, Object requestBody, int requestNbr) {
        HttpResponse<Buffer> response;
        if (requestBody != null) {
            response = restClient.request(method, serverAddr, url)
                    .sendJson(requestBody).toCompletionStage().toCompletableFuture().get();
        } else {
            response = restClient.request(method, serverAddr, url)
                    .send().toCompletionStage().toCompletableFuture().get();
        }
        System.out.printf("%s : status=%s : response: %s%n", requestNbr, response.statusCode(), response.bodyAsString());
        return response;
    }
}
