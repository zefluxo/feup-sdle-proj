package sdle.crdt.implementations;

import lombok.Data;
import sdle.crdt.utils.Pair;

@Data
public class CCounter {

    private String id = "defaultCCounter";
    private DotKernel dotKernel = new DotKernel();

    public CCounter() {
    }

    public CCounter(String id) {
        this.id = id;
    }

    public CCounter(String id, DotKernel dotKernel) {
        this.id = id;
        this.dotKernel = dotKernel;
    }

    public DotContext context() {
        return this.dotKernel.dotContext;
    }

    public CCounter inc(Integer value) {

        CCounter result = new CCounter();
        Integer base = 0;

        for (Pair<String, Integer> key : this.dotKernel.dotMap.keySet()) {

            Integer currValue = this.dotKernel.dotMap.get(key);

            if (key.getFirst() == this.id) {
                base = Math.max(base, currValue);
                result.dotKernel.join(this.dotKernel.remove(key));
            }

        }

        result.dotKernel.join(this.dotKernel.add(id, base + value));
        return result;

    }

    public CCounter dec(Integer value) {

        CCounter result = new CCounter();
        Integer base = 0;

        for (Pair<String, Integer> key : this.dotKernel.dotMap.keySet()) {

            Integer currValue = this.dotKernel.dotMap.get(key);

            if (key.getFirst() == this.id) {
                base = Math.max(base, currValue);
                result.dotKernel.join(this.dotKernel.remove(key));
            }

        }

        Integer newValue = Math.max(base - value, 0);
        result.dotKernel.join(this.dotKernel.add(id, newValue));
        return result;

    }

    public Integer read() {

        Integer result = 0;

        for (Pair<String, Integer> key : this.dotKernel.dotMap.keySet()) {

            Integer value = this.dotKernel.dotMap.get(key);
            result += value;

        }

        return result;

    }

    public void join(CCounter counter) {
        this.dotKernel.join(counter.dotKernel);
    }

    public CCounter reset() {
        CCounter result = new CCounter();
        result.dotKernel = this.dotKernel.removeAllDots();
        return result;
    }

}

