package sdle.crdt.implementations;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// Implementation only allows for mapping Strings to CCounters
@Data
public class ORMap {

    private String id = UUID.randomUUID().toString();
    private DotKernel<String> kernel = new DotKernel<>();
    private Map<String, CCounter> map = new HashMap<>();

    public ORMap() {}

    public ORMap(String id) {
        this.id = id;
    }

    // for replicating
    public ORMap(ORMap map) {
        this.kernel = new DotKernel<String>(map.kernel.context());
        this.map = map.map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public String id() {
        return this.id;
    }

    public Map<String, CCounter> map() {
        return this.map;
    }

    public DotContext context() { return this.kernel.context(); }

    public CCounter get(String key) {

        if (!map.containsKey(key)) this.put(key, 0);
        return this.map.get(key);

    }

    public void put(String key, Integer value) {

        CCounter counter = new CCounter();
        counter.inc(value);
        this.kernel.add(this.id, key);
        this.map.put(key, counter);

    }

    public void remove(String key) {

        if (!this.map.containsKey(key)) return;

        this.kernel.remove(key);
        this.map.remove(key);

    }

    public void inc(String key, Integer value) {
        this.get(key).inc(value);
    }

    public void dec(String key, Integer value) {
        this.get(key).dec(value);
    }


    public void join(ORMap otherMap) {

        Map<String, CCounter> newMap = new HashMap<>();
        this.kernel.join(otherMap.kernel);

        for (String key : kernel.values()) {

            CCounter thisValue = this.map.get(key);
            CCounter otherValue = otherMap.map.get(key);

            if (thisValue == null) newMap.put(key, otherValue);
            else if (otherValue == null) newMap.put(key, thisValue);
            else {
                thisValue.join(otherValue);
                newMap.put(key, thisValue);
            }

        }

        this.map = newMap;

    }

    public void reset() {
        this.kernel.reset();
        this.map.clear();
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(id + ": (");

        for (var entry : this.map.entrySet()) {
            stringBuilder.append("[" + entry.getKey() + " : " + entry.getValue().toString() + "]");
        }

        stringBuilder.append(")");
        return stringBuilder.toString();

    }

}
