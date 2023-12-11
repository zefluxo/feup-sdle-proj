package sdle.crdt.implementations;

import lombok.Data;
import sdle.crdt.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class DotContext {

    public Map<String, Integer> causalContext;

    public DotContext() {
        this.causalContext = new HashMap<>();
    }
    public DotContext(DotContext context) {
        this.causalContext = context.causalContext.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean contains(Pair<String, Integer> key) {
        if (this.causalContext.containsKey(key.getFirst())) {
            return this.causalContext.get(key.getFirst()) >= key.getSecond();
        }
        return false;
    }
    public Integer max(String id) { return this.causalContext.containsKey(id) ? causalContext.get(id) : 0; }
    
    public Integer next(String id) { 
        Integer next = this.max(id) + 1;
        this.causalContext.put(id, next);
        return next; 
    }
    
    public Pair<String, Integer> makeDot(String id) { return new Pair<>(id, this.next(id)); }

    public void join(DotContext context) {

        for (var entry: context.causalContext.entrySet()) {

            String key = entry.getKey();
            Integer value = causalContext.get(key) != null ? 
                            Math.max(entry.getValue(), causalContext.get(key)) : 
                            entry.getValue();

            this.causalContext.put(key, value);
        
        }

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append("Context: (");
        for (var entry : this.causalContext.entrySet()) {
            stringBuilder.append("[" + entry.getKey() + " : " + entry.getValue().toString() + "]");
        }
    
        stringBuilder.append(")");    
        return stringBuilder.toString();
    
    }

}