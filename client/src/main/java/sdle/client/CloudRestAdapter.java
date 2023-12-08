package sdle.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.SneakyThrows;
import sdle.crdt.implementations.ORMap;

public class CloudRestAdapter {
    public static final String SERVER_HOST = System.getenv().getOrDefault("sdle.client.serverHost", "localhost");
    public static final String SERVER_PORT = System.getenv().getOrDefault("sdle.client.serverPort", "7788");
    WebClient restClient;
    ObjectMapper mapper;


    public CloudRestAdapter() {
        restClient = WebClient.create(Vertx.vertx(), new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1).setDefaultPort(Integer.parseInt(SERVER_PORT)).setConnectTimeout(2000));
        mapper = new ObjectMapper();
    }

    @SneakyThrows
    protected static HttpResponse<Buffer> sendSync(WebClient restClient, String serverAddr, HttpMethod method, String url, Object requestBody) {
        HttpResponse<Buffer> response;
        if (requestBody != null) {
            response = restClient.request(method, serverAddr, url).sendJson(requestBody).toCompletionStage().toCompletableFuture().get();
        } else {
            response = restClient.request(method, serverAddr, url).send().toCompletionStage().toCompletableFuture().get();
        }
        //System.out.printf("status=%s : response: %s%n", response.statusCode(), response.bodyAsString());
        return response;
    }

    public void close() {
        restClient.close();
    }

    public HttpResponse<Buffer> sendSync(String serverAddr, HttpMethod method, String url) {
        return sendSync(restClient, serverAddr, method, url, null);
    }

    @SneakyThrows
    public HttpResponse<Buffer> sendSync(String serverAddr, HttpMethod method, String url, Object requestBody) {
        return sendSync(restClient, serverAddr, method, url, requestBody);
    }

    public String newShoppList() {
        HttpResponse<Buffer> response = sendSync(SERVER_HOST, HttpMethod.PUT, "/api/shopp/list");
        return response.bodyAsString();
    }

    @SneakyThrows
    public ORMap getShoppList(String newShoppListHash) {
        HttpResponse<Buffer> response = sendSync(SERVER_HOST, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash));
        return mapper.readValue(response.bodyAsString(), ORMap.class);
    }

    public Boolean incOrDecItem(String hashId, String name, String quantity, boolean isInc) {
        HttpResponse<Buffer> response = sendSync(SERVER_HOST, HttpMethod.POST, String.format("/api/shopp/list/%s/%s/%s/%s", hashId, (isInc ? "inc" : "dec"), name, quantity));
        return response.statusCode() == 200;
    }

    public boolean incItem(String hashId, String name, String quantity) {
        return incOrDecItem(hashId, name, quantity, true);
    }

    public boolean decItem(String hashId, String name, String quantity) {
        return incOrDecItem(hashId, name, quantity, false);
    }
}
