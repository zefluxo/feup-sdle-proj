package sdle.crdt.implementations;

import lombok.Data;
import sdle.crdt.utils.Pair;

import java.util.UUID;

@Data
public class CCounter {

    public DotKernel<Integer> dotKernel;
    private String id;

    public CCounter() {
        this.id = UUID.randomUUID().toString();
        this.dotKernel = new DotKernel<>();
    }

    public CCounter(String id) {
        this.id = id;
        this.dotKernel = new DotKernel<>();
    }

    public CCounter(DotContext context) {
        this.id = UUID.randomUUID().toString();
        this.dotKernel = new DotKernel<>(context);
    }

    public CCounter(String id, DotContext context) {
        this.id = id;
        this.dotKernel = new DotKernel<Integer>(context);
    }

    public CCounter(CCounter otherCounter) {
        this.id = UUID.randomUUID().toString();
        this.dotKernel = otherCounter.dotKernel;
    }

    public DotContext context() {
        return this.dotKernel.context();
    }

    public void inc(Integer value) {
        Integer base = 0;

        for (Pair<String, Integer> key : this.dotKernel.map().keySet()) {
            Integer currValue = this.dotKernel.map().get(key);

            if (key.getFirst().equals(this.id)) {
                base = Math.max(base, currValue);
                this.dotKernel.remove(key);
                break;
            }

        }
        this.dotKernel.add(id, base + value);
    }

    public void dec(Integer value) {
        this.inc(-value);
    }

    public Integer read() {

        Integer result = 0;

        for (Pair<String, Integer> key : this.dotKernel.map().keySet()) {
            Integer value = this.dotKernel.map().get(key);
            result += value;
        }

        return result;

    }

    public void join(CCounter counter) {
        this.dotKernel.join(counter.dotKernel);
    }

    public void reset() {
        this.dotKernel.reset();
    }

    @Override
    public String toString() {
        return this.read().toString();
    }


}

