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
        assertEquals(1, shoppList.get(KEY_ARROZ).read());


        HttpResponse<Buffer> response = sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null, 1);
        String newShoppListHash = response.bodyAsString();
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/inc/%s/%s", newShoppListHash, KEY_ARROZ, 2), null, 3);
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/dec/%s/%s", newShoppListHash, KEY_ARROZ, 1), null, 3);
        //System.out.println(newShoppListHash);
        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null, 2);
        ORMap serializedShoppList = mapper.readValue(response.bodyAsString(), ORMap.class);
        System.out.println(serializedShoppList);

        assertEquals(1, serializedShoppList.get(KEY_ARROZ).read());
        assertEquals(1, serializedShoppList.getMap().size());

        shoppList.join(serializedShoppList);
        assertEquals(2, shoppList.get(KEY_ARROZ).read());
        assertEquals(1, shoppList.getMap().size());


        ORMap shoppList2 = new ORMap("shoppList2");
        shoppList2.inc(KEY_ARROZ, 10);
        shoppList2.dec(KEY_ARROZ, 1);
        shoppList2.dec(KEY_ARROZ, 1);
        shoppList2.inc(KEY_ARROZ, 2);

        System.out.println(shoppList);
        shoppList.join(shoppList2);
        System.out.println(shoppList);

        assertEquals(12, shoppList.get(KEY_ARROZ).read());
        assertEquals(1, shoppList.getMap().size());

    }

    @SneakyThrows
    //@Test
    public void test2() {
        System.out.println("Initializing integration test");

        ORMap shoppList = new ORMap();
        shoppList.inc(KEY_ARROZ, 1);
        shoppList.inc(KEY_FEIJAO, 1);
        assertEquals(1, shoppList.get(KEY_ARROZ).read());
        assertEquals(1, shoppList.get(KEY_FEIJAO).read());


        HttpResponse<Buffer> response = sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null, 1);
        String newShoppListHash = response.bodyAsString();
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/inc/%s/%s", newShoppListHash, KEY_ARROZ, 2), null, 3);
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/inc/%s/%s", newShoppListHash, KEY_FEIJAO, 1), null, 3);
        //System.out.println(newShoppListHash);
        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null, 2);
        ORMap serializedShoppList = mapper.readValue(response.bodyAsString(), ORMap.class);
        System.out.println(serializedShoppList);

        assertEquals(2, serializedShoppList.get(KEY_ARROZ).read());
        assertEquals(1, serializedShoppList.get(KEY_FEIJAO).read());
        assertEquals(2, serializedShoppList.getMap().size());

        shoppList.join(serializedShoppList);
        assertEquals(3, shoppList.get(KEY_ARROZ).read());
        assertEquals(2, shoppList.get(KEY_FEIJAO).read());
        assertEquals(2, shoppList.getMap().size());


        ORMap shoppList2 = new ORMap("shoppList2");
        shoppList2.inc(KEY_ARROZ, 10);
//        shoppList2.dec(KEY_ARROZ, 1);
//        shoppList2.dec(KEY_ARROZ, 1);
        shoppList2.inc(KEY_ARROZ, 2);
        shoppList2.inc(KEY_FEIJAO, 1);
        shoppList2.inc(KEY_FEIJAO, 1);

        System.out.println(shoppList);
        shoppList.join(shoppList2);
        System.out.println(shoppList);

        assertEquals(15, shoppList.get(KEY_ARROZ).read());
        assertEquals(3, shoppList.get(KEY_FEIJAO).read());
        assertEquals(2, shoppList.getMap().size());

    }


//    /**
//     * - Cria uma shopping list (ORMap) via cloud.
//     * - adiciona itens
//     * - modifica a shopping list da cloud
//     * - busca a lista modificada e executa um join com a versao local
//     */
//    @Test
//    @SneakyThrows
//    void generalIntegrationTest() {
//        ExecutorService executor = Executors.newCachedThreadPool();
//        for (int i = 0; i < 55; i++) {
////            executor.submit(this::aMoreComplexJoin);
//            executor.submit(this::test2);
//
////            executor.submit(ClientRestTestIT::test);
//        }
//        executor.awaitTermination(12, TimeUnit.SECONDS);
//    }
}