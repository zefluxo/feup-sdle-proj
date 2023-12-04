package main.java.sdle.crdt.implementations;

import java.util.Map;
import java.util.HashMap;

// Implementation only allows for mapping Strings to CCounters
public class ORMap {

    private String id = "defaultAWORMap";
    private Map<String, CCounter> map = new HashMap<>();
    private DotContext dotContext = new DotContext();

    public ORMap() {}
    public ORMap(String id) { this.id = id; }
    public ORMap(ORMap aworMap) {
        this.dotContext = aworMap.context();
        this.map = aworMap.map();
    }

    public Map<String, CCounter> map() { return this.map; }
    public DotContext context() { return this.dotContext; }

    public CCounter get(String key) {

        if (map.containsKey(key)) return map.get(key);

        CCounter newCounter = new CCounter();
        map.put(key, newCounter);
        return newCounter;

    }

    public void put(String key, CCounter item) { this.map.put(key, item); }
    public ORMap remove(String key) {

        ORMap result = new ORMap();

        if (map.containsKey(key)) {
            CCounter counter = new CCounter();
            counter = map.get(key).reset();
            result.dotContext = counter.context();
            map.remove(key);
        }

        return result;

    }

    public ORMap reset() {

        ORMap result = new ORMap();

        if (!this.map.isEmpty()) {

            for (var entry: this.map.entrySet()) {
                CCounter counter = entry.getValue().reset();
                result.dotContext.join(counter.context());
            }

            this.map.clear();

        }

        return result;

    }

    public void join(ORMap otherMap) {

        DotContext context = this.dotContext;

        for (var entry: this.map.entrySet()) {

            String key = entry.getKey();
            CCounter value = entry.getValue();

            if (!otherMap.map().containsKey(key)) {

                CCounter empty = new CCounter(this.id, new DotKernel(otherMap.context()));
                value.join(empty);
                this.dotContext = context;

            }

        }

        for (var entry: otherMap.map().entrySet()) {

            String key = entry.getKey();
            CCounter value = entry.getValue();

            this.get(key).join(value);
            this.dotContext = context;

        }

        this.dotContext.join(otherMap.context());

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(id + ": (");

        for (var entry: this.map.entrySet()) {
            stringBuilder.append("[" + entry.getKey().toString() + " : " + entry.getValue().read() + "]");
        }

        stringBuilder.append(")");
        return stringBuilder.toString();

    }

}
