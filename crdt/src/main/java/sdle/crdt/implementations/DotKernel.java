package sdle.crdt.implementations;

import lombok.Data;
import sdle.crdt.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DotKernel<V> {

    private Map<Pair<String, Integer>, V> dotMap = new HashMap<>();
    private DotContext context;

    public DotKernel() {
        this.context = new DotContext();
    }

    public DotKernel(DotContext context) {
        this.context = new DotContext(context);
    }

    public DotKernel(DotKernel<V> kernel) {
        this.dotMap = kernel.dotMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.context = new DotContext(kernel.context);
    }

    public Map<Pair<String, Integer>, V> map() {
        return this.dotMap;
    }

    public DotContext context() {
        return this.context;
    }

    public void setContext(DotContext context) {
        this.context = context;
    }

    public Set<V> values() {

        return dotMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

    }

    public void add(String id, V value) {
        Pair<String, Integer> dot = context.makeDot(id);
        this.dotMap.put(dot, value);
        System.out.println(dotMap);
    }

    public void remove(Pair<String, Integer> key) {
        this.dotMap.remove(key);
    }

    public void remove(V value) {

        var iterator = this.dotMap.entrySet().iterator();
        while (iterator.hasNext()) {
            V entryValue = iterator.next().getValue();
            if (entryValue == value) iterator.remove();
        }

    }

    public void join(DotKernel<V> kernel) {
        this.join(kernel, true);
    }

    public void join(DotKernel<V> kernel, boolean mergeContext) {

        var iterator = this.dotMap.entrySet().iterator();
        while (iterator.hasNext()) {

            var entry = iterator.next();
            Pair<String, Integer> key = entry.getKey();

            boolean keyInOther = !kernel.dotMap.keySet().stream()
                    .filter(k -> k.getFirst().equals(key.getFirst())).toList().isEmpty();

            boolean otherKnowsKey = kernel.context.contains(key.getFirst());

            if (!keyInOther && otherKnowsKey) iterator.remove();

        }

        iterator = kernel.dotMap.entrySet().iterator();
        while (iterator.hasNext()) {

            var entry = iterator.next();
            Pair<String, Integer> key = entry.getKey();
            V value = entry.getValue();

            List<Pair<String, Integer>> matchingKeys = this.dotMap.keySet().stream()
                    .filter(k -> k.getFirst().equals(key.getFirst())).toList();

            boolean keyInThis = !matchingKeys.isEmpty();

            boolean thisKnowsKey = this.context.contains(key.getFirst());

            if (keyInThis) {
                this.dotMap.put(key, value);
            } else if (!thisKnowsKey) this.dotMap.put(key, value);

        }

        if (mergeContext) context.join(kernel.context);
    }

    public void reset() {
        this.dotMap.clear();
    }

}