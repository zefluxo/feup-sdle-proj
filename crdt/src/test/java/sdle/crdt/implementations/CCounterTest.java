package sdle.crdt.implementations;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class CCounterTest {

    @Test
    void testCreateNewCounterAndRead() {
        CCounter counter = new CCounter();
        Assertions.assertEquals(0, counter.read());
    }

    @Test
    void testInc() {
        CCounter counter = new CCounter();
        counter.inc(4);
        Assertions.assertEquals(4, counter.read());
    }

    @Test
    void dec() {
        CCounter counter = new CCounter();
        counter.inc(4);
        counter.dec(1);
        Assertions.assertEquals(3, counter.read());
    }

    @Test
    void read() {
        CCounter counter = new CCounter();
        Assertions.assertEquals(0, counter.read());
    }

    @Test
    void join() {
        CCounter counter1 = new CCounter(UUID.randomUUID().toString());
        counter1.inc(10);
        counter1.dec(2);
        counter1.inc(1);
        Assertions.assertEquals(9, counter1.read());

        CCounter counter2 = new CCounter(UUID.randomUUID().toString());
        counter2.inc(2);
        counter2.dec(1);
        counter2.inc(1);
        Assertions.assertEquals(2, counter2.read());

        counter1.join(counter2);
        Assertions.assertEquals(11, counter1.read());
        Assertions.assertEquals(2, counter2.read());
    }


    @SneakyThrows
    @Test
    public void aMoreComplexJoin() {

        CCounter counter1 = new CCounter();
        counter1.inc(2);
        counter1.dec(1);
        Assertions.assertEquals(1, counter1.read());

        CCounter counter2 = new CCounter();
        counter2.dec(2);
        counter2.inc(2);
        Assertions.assertEquals(2, counter2.read());

        counter2.join(counter1);
        Assertions.assertEquals(3, counter2.read());


        CCounter counter3 = new CCounter();
        counter3.inc(10);
        counter3.dec(1);
        counter3.dec(1);
        counter3.inc(2);
        counter3.join(counter2);
        Assertions.assertEquals(13, counter3.read());

    }

    @Test
    void reset() {
        CCounter counter1 = new CCounter("counter1");
        counter1.inc(10);
        counter1.dec(2);
        counter1.inc(1);
        Assertions.assertEquals(9, counter1.read());

        counter1.reset();
        Assertions.assertEquals(0, counter1.read());
    }
}