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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sdle.crdt.implementations.ORMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sdle.client.ClientRest.sendSync;

public class ClientRestTestIT {

    public static final String SERVER_ADDR = System.getenv().getOrDefault("sdle.client.serverAddr", "localhost");
    public static final String KEY_FEIJAO = "feijao";
    public static final String KEY_ARROZ = "arroz";
    static WebClient restClient;
    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
        restClient = WebClient.create(Vertx.vertx(), new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1).setDefaultPort(7788).setConnectTimeout(2000));
    }


    @SneakyThrows
    @Test
    public void test1() {
        System.out.println("Initializing integration test");

        ORMap shoppList = new ORMap();
        shoppList.inc(KEY_ARROZ, 1);
        shoppList.inc(KEY_FEIJAO, 1);
        assertEquals(1, shoppList.get(KEY_ARROZ).read());
        assertEquals(1, shoppList.get(KEY_FEIJAO).read());


        HttpResponse<Buffer> response = sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null, 1);
        String newShoppListHash = response.bodyAsString();
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/%s/%s", newShoppListHash, KEY_ARROZ, 2), null, 3);
        //System.out.println(newShoppListHash);
        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null, 2);
        ORMap serializedShoppList = mapper.readValue(response.bodyAsString(), ORMap.class);
        assertEquals(2, serializedShoppList.get(KEY_ARROZ).read());
        assertEquals(1, serializedShoppList.getMap().size());

        shoppList.join(serializedShoppList);
        assertEquals(3, shoppList.get(KEY_ARROZ).read());
        assertEquals(1, shoppList.get(KEY_FEIJAO).read());
        assertEquals(2, shoppList.getMap().size());


        ORMap shoppList2 = new ORMap("shoppList2");
        shoppList2.inc(KEY_ARROZ, 10);
        shoppList2.dec(KEY_ARROZ, 1);
        shoppList2.dec(KEY_ARROZ, 1);
        shoppList2.inc(KEY_ARROZ, 2);
        shoppList2.inc(KEY_FEIJAO, 1);
        shoppList2.inc(KEY_FEIJAO, 1);

//        System.out.println(shoppList);
        shoppList.join(shoppList2);
//        System.out.println(shoppList);

        assertEquals(13, shoppList.get(KEY_ARROZ).read());
        assertEquals(3, shoppList.get(KEY_FEIJAO).read());
        assertEquals(2, shoppList.getMap().size());

    }


    @SneakyThrows
    @Test
    public void test2() {
        System.out.println("Initializing integration test");

        HttpResponse<Buffer> response = sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null, 1);
        String newShoppListHash = response.bodyAsString();
        //System.out.println(newShoppListHash);

        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/%s/%s", newShoppListHash, KEY_ARROZ, 4), null, 3);
        ORMap shopList = new ORMap();
        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null, 2);

        ORMap otherMap1 = mapper.readValue(response.bodyAsString(), ORMap.class);
        shopList.join(otherMap1);
        System.out.println("SHOPP: " + shopList);
        //System.out.println(response.bodyAsString());

        shopList.put("feijao", 1);
        shopList.inc(KEY_ARROZ, 2);
        shopList.dec(KEY_ARROZ, 1);
        assertEquals(1, shopList.get("feijao").read());
        assertEquals(5, shopList.get(KEY_ARROZ).read());

        System.out.println("SHOPP: " + shopList);
        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null, 4);
        ORMap otherMap = mapper.readValue(response.bodyAsString(), ORMap.class);
        shopList.inc(KEY_ARROZ, 3);
        System.out.println("SHOPP: " + shopList);
        System.out.println("OTHER: " + otherMap);
        System.out.println("OTHER: " + otherMap.map().get(KEY_ARROZ).read());
        System.out.println("NEW SHOPP: " + shopList.map().get(KEY_ARROZ).read());
        otherMap.join(shopList);
//        shopList.join(otherMap);
        System.out.println("OTHER: " + otherMap.map().get(KEY_ARROZ).read());
        System.out.println("NEW SHOPP: " + shopList.map().get(KEY_ARROZ).read());
        System.out.println("OTHER: " + otherMap.get(KEY_ARROZ).read());
        System.out.println("NEW SHOPP: " + shopList.get(KEY_ARROZ).read());
////        shopList.join(otherMap);
//        System.out.println("NEW SHOPP: " + shopList);
//        System.out.println("OTHER: " + otherMap);
//        ORMap orMap2 = new ORMap();
//        orMap2.join(otherMap);
//        System.out.println("NEW SHOPP 2: " + shopList);
//        orMap2.join(shopList);
//        System.out.println("NEW SHOPP 2 after join : " + orMap2);

        assertEquals(1, otherMap.get(KEY_FEIJAO).read());
        assertEquals(7, otherMap.get(KEY_ARROZ).read());
    }

    /**
     * - Cria uma shopping list (ORMap) via cloud.
     * - adiciona itens
     * - modifica a shopping list da cloud
     * - busca a lista modificada e executa um join com a versao local
     */
//    @Test
//    @SneakyThrows
//    void generalIntegrationTest() {
//        ExecutorService executor = Executors.newCachedThreadPool();
//        for (int i = 0; i < 5; i++) {
//            executor.submit(ClientRestTestIT::test);
//        }
//        executor.awaitTermination(2, TimeUnit.SECONDS);
//    }
}