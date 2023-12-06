package sdle.crdt.implementations;

import lombok.Data;
import sdle.crdt.utils.Pair;

import java.util.HashMap;
import java.util.Map;


@Data
public class DotKernel {

    public Map<Pair<String, Integer>, Integer> dotMap = new HashMap<>();
    public DotContext dotContext = new DotContext();

    public DotKernel() {
    }

    public DotKernel(DotContext dotContext) {
        this.dotContext = dotContext;
    }

    public void join(DotKernel dotKernel) {

        if (this == dotKernel) return;

        var iterator = this.dotMap.entrySet().iterator();
        while (iterator.hasNext()) {

            var entry = iterator.next();
            Pair<String, Integer> key = entry.getKey();

            boolean keyInOther = dotKernel.dotMap.containsKey(key);
            boolean otherKnowsKey = dotKernel.dotContext.dotIn(key);

            if (!keyInOther && otherKnowsKey) iterator.remove();

        }

        iterator = dotKernel.dotMap.entrySet().iterator();
        while (iterator.hasNext()) {

            var entry = iterator.next();
            Pair<String, Integer> key = entry.getKey();
            Integer value = entry.getValue();

            boolean keyIsHere = this.dotMap.containsKey(key);
            boolean thisKnowsKey = this.dotContext.dotIn(key);

            if (!keyIsHere && !thisKnowsKey) this.dotMap.put(key, value);

        }

        dotContext.join(dotKernel.dotContext);

    }

    public DotKernel add(String id, Integer value) {

        DotKernel result = new DotKernel();

        Pair<String, Integer> dot = this.dotContext.makeDot(id);
        this.dotMap.put(dot, value);

        result.dotMap.put(dot, value);
        result.dotContext.insertDot(dot, false);
        return result;

    }

    // remove all dots matching given value
    public DotKernel remove(Integer value) {

        DotKernel result = new DotKernel();

        var iterator = this.dotMap.entrySet().iterator();
        while (iterator.hasNext()) {

            var entry = iterator.next();
            Pair<String, Integer> entryKey = entry.getKey();
            Integer entryValue = entry.getValue();

            if (entryValue == value) {

                result.dotContext.insertDot(entryKey, false);
                iterator.remove();

            }

        }

        result.dotContext.compact();
        return result;

    }

    public DotKernel remove(Pair<String, Integer> dot) {

        DotKernel result = new DotKernel();
        boolean dotExists = this.dotMap.containsKey(dot);

        if (dotExists) {
            result.dotContext.insertDot(dot, true);
            this.dotMap.remove(dot);
        }

        return result;

    }

    // self-explanatory: remove all dots from dotMap
    public DotKernel removeAllDots() {

        DotKernel result = new DotKernel();

        var iterator = this.dotMap.entrySet().iterator();
        while (iterator.hasNext()) {

            var entry = iterator.next();
            result.dotContext.insertDot(entry.getKey(), false);

        }

        result.dotContext.compact();
        this.dotMap.clear();
        return result;

    }

}