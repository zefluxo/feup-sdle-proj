package sdle.crdt.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import sdle.crdt.utils.Pair;
import sdle.crdt.utils.PairKeyDeserializer;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ORMapTest {

    public static final String KEY_ARROZ = "arroz";
    public static final String KEY_FEIJAO = "feijao";

    @Test
    void map() {
        ORMap orMap = new ORMap();
        CCounter counter = new CCounter();
        counter.inc(2);
        orMap.put(KEY_ARROZ, counter);
        Map<String, CCounter> map = orMap.map();
        assertEquals(1, map.size());
        assertTrue(map.containsKey(KEY_ARROZ));
        assertEquals(2, map.get(KEY_ARROZ).read());
    }

    @Test
    void context() {
        ORMap orMap = new ORMap("orMap1");
        CCounter counter = new CCounter("arroz1");
        counter.inc(2);
        counter.dec(1);
        orMap.put(KEY_ARROZ, counter);
        assertEquals("orMap1: ([arroz : 1])", orMap.toString());
        // TODO: realvaliar esse resultado ... o context deveria refletir  o put ???
        assertEquals("Context\nCC ()\nDC ()", orMap.context().toString());
    }

    @Test
    void get() {
        ORMap orMap = new ORMap("orMap1");
        CCounter counter = new CCounter("arroz1");
        counter.inc(2);
        orMap.put(KEY_ARROZ, counter);
        assertEquals("orMap1: ([arroz : 2])", orMap.toString());
    }

    @Test
    void put() {
        ORMap orMap = new ORMap("orMap1");
        CCounter counter = new CCounter("arroz1");
        counter.inc(2);
        orMap.put(KEY_ARROZ, counter);
    }

    @Test
    void remove() {
        ORMap orMap = new ORMap("orMap1");
        CCounter counter = new CCounter("arroz1");
        counter.inc(2);
        orMap.put(KEY_ARROZ, counter);
        assertEquals(1, orMap.map().size());
        orMap.put(KEY_FEIJAO, new CCounter());
        assertEquals(2, orMap.map().size());
        orMap.remove(KEY_ARROZ);
        assertEquals("orMap1: ([feijao : 0])", orMap.toString());
    }

    @Test
    void reset() {
    }

    @Test
    void join() {

        ORMap orMap1 = new ORMap(UUID.randomUUID().toString());
        orMap1.put(KEY_ARROZ, new CCounter(UUID.randomUUID().toString()));
        orMap1.get(KEY_ARROZ).inc(2);
        CCounter feijaoCounter = new CCounter(UUID.randomUUID().toString());
        feijaoCounter.inc(1);
        orMap1.put(KEY_FEIJAO, feijaoCounter);
        feijaoCounter.inc(2);
        feijaoCounter.dec(1);
        feijaoCounter.inc(1);


        ORMap orMap2 = new ORMap(UUID.randomUUID().toString());
        orMap2.put(KEY_FEIJAO, new CCounter(UUID.randomUUID().toString()));
        orMap2.get(KEY_FEIJAO).inc(2);
        orMap2.get(KEY_FEIJAO).dec(1);
        orMap2.remove(KEY_FEIJAO);
        orMap2.put(KEY_FEIJAO, new CCounter(UUID.randomUUID().toString()));
        orMap2.get(KEY_FEIJAO).inc(1);

        orMap1.join(orMap2);
        assertEquals(2, orMap1.map().size());

        assertAll(
                () -> assertEquals(4, orMap1.get(KEY_FEIJAO).read()),
                () -> assertEquals(2, orMap1.get(KEY_ARROZ).read())
        );
    }

    @Test
    void testSerializeDeserialiseJSon() throws JsonProcessingException {
        ORMap orMap1 = new ORMap(UUID.randomUUID().toString());
        orMap1.put(KEY_ARROZ, new CCounter(UUID.randomUUID().toString()));
        orMap1.get(KEY_ARROZ).inc(3);
        orMap1.get(KEY_ARROZ).dec(1);
        orMap1.get(KEY_ARROZ).inc(2);

//        ORMap orMap1 = new ORMap();

        SimpleModule nioModule = new SimpleModule();
        nioModule.addKeyDeserializer(Pair.class, new PairKeyDeserializer());

        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(nioModule);

        String orMapSerialized = mapper.writeValueAsString(orMap1);
        System.out.println(orMapSerialized);
        ORMap orMap2 = mapper.readValue(orMapSerialized, ORMap.class);
        assertEquals(4, orMap1.get(KEY_ARROZ).read());
        assertEquals(4, orMap2.get(KEY_ARROZ).read());
    }
}

