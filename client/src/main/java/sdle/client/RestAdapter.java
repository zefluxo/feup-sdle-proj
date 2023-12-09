package sdle.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class RestAdapter {
    public static final String SERVER_HOST = System.getenv().getOrDefault("sdle.client.serverHost", "localhost");
    public static final String SERVER_PORT = System.getenv().getOrDefault("sdle.client.serverPort", "7788");
    WebClient restClient;


    public RestAdapter() {
        restClient = WebClient.create(Vertx.vertx(), new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1).setDefaultPort(Integer.parseInt(SERVER_PORT)).setConnectTimeout(2000));
    }

    protected static Optional<HttpResponse<Buffer>> sendSync(WebClient restClient, String serverAddr, HttpMethod method, String url, Object requestBody) {
        HttpResponse<Buffer> response;
        try {
            if (requestBody != null) {
                response = restClient.request(method, serverAddr, url).sendJson(requestBody).toCompletionStage().toCompletableFuture().get();
            } else {
                response = restClient.request(method, serverAddr, url).send().toCompletionStage().toCompletableFuture().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
        //System.out.printf("status=%s : response: %s%n", response.statusCode(), response.bodyAsString());
        return Optional.ofNullable(response);
    }

    public Optional<HttpResponse<Buffer>> sendSync(String serverAddr, HttpMethod method, String url) {
        return sendSync(restClient, serverAddr, method, url, null);
    }

    @SneakyThrows
    public Optional<HttpResponse<Buffer>> sendSync(String serverAddr, HttpMethod method, String url, Object requestBody) {
        return sendSync(restClient, serverAddr, method, url, requestBody);
    }

}
