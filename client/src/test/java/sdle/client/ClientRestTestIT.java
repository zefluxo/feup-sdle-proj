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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import sdle.crdt.implementations.ORMap;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sdle.client.RestAdapter.sendSync;

public class ClientRestTestIT {

    public static final String SERVER_ADDR = System.getenv().getOrDefault("sdle.client.serverAddr", "localhost");
    public static final String KEY_FEIJAO = "feijao";
    public static final String KEY_ARROZ = "arroz";
    static WebClient restClient;
    static ObjectMapper mapper;

    static String hashId;

    static String oldUserDir;

    @AfterAll
    static void tearDown() {
        System.setProperty("user.dir", oldUserDir);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    void setup() {
        oldUserDir = System.getProperty("user.dir");
        mapper = new ObjectMapper();
        restClient = WebClient.create(Vertx.vertx(), new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1).setDefaultPort(7788).setConnectTimeout(2000));
        File dataDir = Paths.get("target", "data").toFile();
        dataDir.mkdirs();
        String[] entries = dataDir.list();
        assert entries != null;
        for (String s : entries) {
            File currentFile = new File(dataDir.getPath(), s);
            currentFile.delete();
        }
        System.setProperty("user.dir", "target");
        assert 0 == new CommandLine(new ClientApp()).execute("new");
        hashId = Objects.requireNonNull(dataDir.list())[0];
    }

    @SneakyThrows
    @Test
    public void testNoArgsCommand() {
        assert 0 == new CommandLine(new ClientApp()).execute();
    }

    @SneakyThrows
    @Test
    public void testNewList() {
        assert 0 == new CommandLine(new ClientApp()).execute("new");
    }

    @SneakyThrows
    @Test
    public void testGetList() {
        assert 0 == new CommandLine(new ClientApp()).execute("getList", "-id=" + hashId);
    }

    @SneakyThrows
    @Test
    public void testIncItem() {
        assert 0 == new CommandLine(new ClientApp()).execute("incItem", "-id=" + hashId, "-n=" + KEY_ARROZ, "-q=10");
    }

    @SneakyThrows
    @Test
    public void testDecItem() {
        assert 0 == new CommandLine(new ClientApp()).execute("decItem", "-id=" + hashId, "-n=" + KEY_FEIJAO, "-q=1");
    }

    @SneakyThrows
    @Test
    public void testListAll() {
        assert 0 == new CommandLine(new ClientApp()).execute("all");
    }

    @SneakyThrows
    @Test
    public void integrationTestUsingCommands() {
        System.out.println("Initializing integration test");

        ORMap shoppList = new ORMap();
        shoppList.inc(KEY_ARROZ, 1);
        assertEquals(1, shoppList.get(KEY_ARROZ).read());

        CommandLine commandLine = new CommandLine(new ClientApp());
        commandLine.execute("incItem", "-id=" + hashId, "-n=" + KEY_ARROZ, "-q=2");
        commandLine.execute("decItem", "-id=" + hashId, "-n=" + KEY_ARROZ, "-q=1");

        ORMap serializedShoppList = new LocalStorage().getLocalShoppLists().get(hashId);
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
    @Test
    public void integrationTestUsingRestOnly() {
        System.out.println("Initializing integration test");

        ORMap shoppList = new ORMap();
        shoppList.inc(KEY_ARROZ, 1);
        assertEquals(1, shoppList.get(KEY_ARROZ).read());

        Optional<HttpResponse<Buffer>> response = sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null);
        if (response.isEmpty()) return; // if cloud is not online, no need to continue test
        String newShoppListHash = response.get().bodyAsString();

        System.out.println(newShoppListHash);
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/inc/%s/%s", newShoppListHash, KEY_ARROZ, 2), null);
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/dec/%s/%s", newShoppListHash, KEY_ARROZ, 1), null);
        //System.out.println(newShoppListHash);
        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null);
        ORMap serializedShoppList = mapper.readValue(response.get().bodyAsString(), ORMap.class);
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


        HttpResponse<Buffer> response = sendSync(restClient, SERVER_ADDR, HttpMethod.PUT, "/api/shopp/list", null).get();
        String newShoppListHash = response.bodyAsString();
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/inc/%s/%s", newShoppListHash, KEY_ARROZ, 2), null);
        sendSync(restClient, SERVER_ADDR, HttpMethod.POST, String.format("/api/shopp/list/%s/inc/%s/%s", newShoppListHash, KEY_FEIJAO, 1), null);
        //System.out.println(newShoppListHash);
        response = sendSync(restClient, SERVER_ADDR, HttpMethod.GET, String.format("/api/shopp/list/%s", newShoppListHash), null).get();
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