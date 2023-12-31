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
        orMap.put(KEY_ARROZ, 1);
        orMap.inc(KEY_ARROZ, 2);
        Map<String, CCounter> map = orMap.map();
        assertEquals(1, map.size());
        assertTrue(map.containsKey(KEY_ARROZ));
        assertEquals(3, map.get(KEY_ARROZ).read());
    }

    @Test
    void inc() {
        ORMap orMap = new ORMap("orMap1");
        orMap.inc(KEY_ARROZ, 1);
        assertEquals(1, orMap.get(KEY_ARROZ).read());
    }

    @Test
    void dec() {
        ORMap orMap = new ORMap("orMap1");
        orMap.put(KEY_ARROZ, 10);
        orMap.dec(KEY_ARROZ, 1);
        assertEquals(9, orMap.get(KEY_ARROZ).read());
    }

    @Test
    void get() {
        ORMap orMap = new ORMap();
        orMap.inc(KEY_ARROZ, 2);
        assertEquals(2, orMap.get(KEY_ARROZ).read());
    }

    @Test
    void put() {
        ORMap orMap = new ORMap();
        orMap.put(KEY_ARROZ, 2);
        assertEquals(2, orMap.get(KEY_ARROZ).read());

    }

    @Test
    void remove() {
        ORMap orMap = new ORMap();
        orMap.inc(KEY_ARROZ, 2);
        assertEquals(1, orMap.map().size());
        orMap.put(KEY_FEIJAO, 0);
        assertEquals(2, orMap.map().size());
        orMap.remove(KEY_ARROZ);
        assertEquals(1, orMap.getMap().size());
    }

    @Test
    void reset() {
    }

    @Test
    void join() {

        ORMap orMap1 = new ORMap();
        orMap1.put(KEY_ARROZ, 0);
        orMap1.inc(KEY_ARROZ, 2);
        orMap1.inc(KEY_FEIJAO, 1);
        orMap1.inc(KEY_FEIJAO, 2);
        orMap1.dec(KEY_FEIJAO, 1);
        orMap1.inc(KEY_FEIJAO, 1);

        assertAll(() -> assertEquals(3, orMap1.get(KEY_FEIJAO).read()), () -> assertEquals(2, orMap1.get(KEY_ARROZ).read()));

        ORMap orMap2 = new ORMap();
        orMap2.put(KEY_FEIJAO, 0);
        orMap2.get(KEY_FEIJAO).inc(2);
        orMap2.get(KEY_FEIJAO).dec(1);
        orMap2.remove(KEY_FEIJAO);
        orMap2.put(KEY_FEIJAO, 0);
        orMap2.get(KEY_FEIJAO).inc(1);


        System.out.println(orMap1);
        orMap1.join(orMap2);
        System.out.println(orMap1);
        assertEquals(2, orMap1.map().size());

        assertAll(() -> assertEquals(4, orMap1.get(KEY_FEIJAO).read()), () -> assertEquals(2, orMap1.get(KEY_ARROZ).read()));
    }


    //@Test
    public void aMoreComplexJoin() {

        ORMap shoppList = new ORMap();
        shoppList.inc(KEY_ARROZ, 1);
        shoppList.inc(KEY_FEIJAO, 2);
        assertEquals(1, shoppList.get(KEY_ARROZ).read());
        assertEquals(2, shoppList.get(KEY_FEIJAO).read());

        ORMap shoppList2 = new ORMap();
        shoppList2.inc(KEY_ARROZ, 2);
        shoppList2.inc(KEY_FEIJAO, 3);
        assertEquals(2, shoppList2.get(KEY_ARROZ).read());
        assertEquals(3, shoppList2.get(KEY_FEIJAO).read());
        assertEquals(2, shoppList2.getMap().size());

        shoppList2.join(shoppList);
        assertEquals(3, shoppList2.get(KEY_ARROZ).read());
        assertEquals(5, shoppList2.get(KEY_FEIJAO).read());
        assertEquals(2, shoppList2.getMap().size());


        ORMap shoppList3 = new ORMap();
        shoppList3.inc(KEY_ARROZ, 10);
        shoppList3.dec(KEY_ARROZ, 1);
        shoppList3.dec(KEY_ARROZ, 1);
        shoppList3.inc(KEY_ARROZ, 2);
        shoppList3.inc(KEY_FEIJAO, 1);
        shoppList3.inc(KEY_FEIJAO, 1);

        shoppList2.join(shoppList3);
//        System.out.println(shoppList3);

        assertAll(
                () -> assertEquals(13, shoppList2.getMap().get(KEY_ARROZ).read()),
                () -> assertEquals(7, shoppList2.getMap().get(KEY_FEIJAO).read()),
                () -> assertEquals(2, shoppList2.getMap().size())
        );


    }


    @Test
    void testSerializeDeserialiseJSon() throws JsonProcessingException {
        ORMap orMap1 = new ORMap(UUID.randomUUID().toString());
        orMap1.put(KEY_ARROZ, 0);
        orMap1.get(KEY_ARROZ).inc(3);
        orMap1.get(KEY_ARROZ).dec(1);
        orMap1.get(KEY_ARROZ).inc(2);

//        ORMap orMap1 = new ORMap();

        SimpleModule nioModule = new SimpleModule();
        nioModule.addKeyDeserializer(Pair.class, new PairKeyDeserializer());
//        nioModule.addSerializer(Pair.class, new PairKeySerializer());

        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(nioModule);

        String orMapSerialized = mapper.writeValueAsString(orMap1);
        System.out.println(orMapSerialized);
        ORMap orMap2 = mapper.readValue(orMapSerialized, ORMap.class);

        assertEquals(4, orMap1.get(KEY_ARROZ).read());
        assertEquals(4, orMap2.get(KEY_ARROZ).read());
        String orMapSerialized2 = mapper.writeValueAsString(orMap2);
        System.out.println(orMapSerialized2);
        System.out.println(orMap1);
        System.out.println(orMap2);
        orMap1.join(orMap2);
        System.out.println(orMap1);
    }

    @Test
    public void anEvenMoreComplexJoin() {

        ORMap map1 = new ORMap("A");
        ORMap map2 = new ORMap("B");

        map1.put("x", 2);

        map2.join(map1);
        assertEquals(2, map2.get("x").read());

        map2.remove("x");
        map2.put("x", 1);

        assertEquals(1, map2.get("x").read());

        map1.inc("x", 1);
        assertEquals(3, map1.get("x").read());

        map1.join(map2);
        assertEquals(4, map1.get("x").read());

    }

    @Test
    public void joinOnMultipleKeys() {

        ORMap map1 = new ORMap("A");
        ORMap map2 = new ORMap("B");

        map1.put("a", 1);
        map1.put("b", 2);
        map1.put("c", 3);
        map2.put("a", 3);
        map2.put("b", 4);
        map2.put("c", 6);

        map1.join(map2);
        System.out.println(map1);

    }

    @Test
    public void test() {

        ORMap map = new ORMap();
        ORMap map1 = new ORMap(map);

        map1.inc("feijao", 1);
        map.join(map1);

        ORMap map2 = new ORMap(map);
        System.out.println(map.getKernel());
        assertEquals(1, map.get("feijao").read());

        map2.inc("feijao", 2);
        //map.put("arroz", 3);
        map.join(map2);
        System.out.println(map.getKernel());

        assertEquals(3, map.get("feijao").read());
        //assertEquals(3, map.get("arroz").read());

    }


}

