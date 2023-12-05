package sdle.crdt.implementations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;
import java.util.UUID;

class ORMapTest {

    @Test
    void map() {
        ORMap orMap = new ORMap();
        orMap.put("arroz", new CCounter().inc(1));
        orMap.get("arroz").inc(1);
        Assertions.assertEquals(2, orMap.get("arroz").read());
    }

    @Test
    void context() {
    }

    @Test
    void get() {
        TreeSet set = new TreeSet<>();
        set.add(2);
        set.add(3);
        set.add(4);
        Assertions.assertEquals(3, set.higher(2));
    }

    @Test
    void put() {
    }

    @Test
    void remove() {
    }

    @Test
    void reset() {
    }

    @Test
    void join() {

        ORMap orMap1 = new ORMap(UUID.randomUUID().toString());
        orMap1.put("arroz", new CCounter(UUID.randomUUID().toString()));
        orMap1.get("arroz").inc(2);
        CCounter feijaoCounter = new CCounter(UUID.randomUUID().toString());
        feijaoCounter.inc(1);
        orMap1.put("feijao", feijaoCounter);
        System.out.println(orMap1.map().get("feijao").context());
        feijaoCounter.inc(1);
        System.out.println(orMap1.map().get("feijao").context());
        feijaoCounter.inc(1);
        System.out.println(orMap1.map().get("feijao").context());
        feijaoCounter.inc(1);
        System.out.println(orMap1.map().get("feijao").context());


        ORMap orMap2 = new ORMap(UUID.randomUUID().toString());
        orMap2.put("feijao", new CCounter(UUID.randomUUID().toString()));
        orMap2.get("feijao").inc(2);
        orMap2.get("feijao").inc(1);
        orMap2.remove("feijao");
        orMap2.put("feijao", new CCounter(UUID.randomUUID().toString()));
        orMap2.get("feijao").inc(1);

        orMap1.join(orMap2);
        System.out.println(orMap1.map().get("feijao").context());
        Assertions.assertEquals(2, orMap1.map().size());

        Assertions.assertEquals(5, orMap1.get("feijao").read());
        Assertions.assertEquals(2, orMap1.get("arroz").read());
    }
}