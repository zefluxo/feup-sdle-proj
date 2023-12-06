package sdle.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sdle.crdt.implementations.CCounter;
import sdle.crdt.implementations.ORMap;
import sdle.crdt.utils.Pair;
import sdle.crdt.utils.PairKeyDeserializer;

import java.util.UUID;

import static sdle.client.ClientRest.sendSync;

public class ClientRestTestIT {

    public static final String SERVER_ADDR = System.getenv().getOrDefault("sdle.client.serverAddr", "localhost");
    static WebClient restClient;
    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        SimpleModule nioModule = new SimpleModule();
        nioModule.addKeyDeserializer(Pair.class, new PairKeyDeserializer());

        mapper = new ObjectMapper();
        mapper.registerModule(nioModule);

        restClient = WebClient.create(Vertx.vertx(), new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2).setDefaultPort(7788));
    }

    /**
     * - Cria uma shopping list (ORMap) via cloud.
     * - adiciona itens
     * - modifica a shopping list da cloud
     * - busca a lista modificada e executa um join com a versao local
     */
    @Test
    @SneakyThrows
    void generalIntegrationTest() {

        System.out.println("Initializing integration test");
        HttpResponse<Buffer> response = sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null, 1);
        String newShoppListHash = response.bodyAsString();
        //System.out.println(newShoppListHash);


        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null, 2);
        ORMap shopList = mapper.readValue(response.bodyAsString(), ORMap.class);
        System.out.println(shopList);

        response = sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/%s/%s", newShoppListHash, "arroz", 2), null, 3);
        //System.out.println(response.bodyAsString());

        CCounter counter = new CCounter(UUID.randomUUID().toString());
        shopList.put("feijao", new CCounter(UUID.randomUUID().toString()));

        shopList.put("arroz", counter.inc(1));
        Assertions.assertEquals(0, shopList.get("feijao").read());
        Assertions.assertEquals(1, shopList.get("arroz").read());

        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null, 4);
        shopList.join(mapper.readValue(response.bodyAsString(), ORMap.class));
        System.out.println(shopList);

        Assertions.assertEquals(0, shopList.get("feijao").read());
        Assertions.assertEquals(3, shopList.get("arroz").read());
    }
}