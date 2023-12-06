package sdle.crdt.implementations;

import lombok.Data;
import sdle.crdt.utils.Pair;

import java.util.*;

@Data
public class DotContext {

    public Map<String, Integer> causalContext = new HashMap<>();
    public Set<Pair<String, Integer>> dotCloud = new HashSet<>();

    public boolean dotIn(Pair<String, Integer> dot) {

        Integer foundChange = causalContext.get(dot.getFirst());

        if (foundChange == null) return false;
        if (dot.getSecond() <= foundChange) return true;
        return dotCloud.contains(dot);

    }

    public Pair<String, Integer> makeDot(String id) {

        Integer value = 1;

        if (this.causalContext.containsKey(id)) value = this.causalContext.get(id) + 1;

        this.causalContext.put(id, value);
        return new Pair<>(id, value);

    }

    public void insertDot(Pair<String, Integer> dot, boolean compact) {
        this.dotCloud.add(dot);
        if (compact) this.compact();
    }

    public void compact() {

        boolean flag;
        do {

            flag = false;

            Iterator<Pair<String, Integer>> iterator = dotCloud.iterator();
            while (iterator.hasNext()) {

                Pair<String, Integer> dot = iterator.next();

                String dotID = dot.getFirst();
                Integer dotValue = dot.getSecond();

                boolean dotExists = this.causalContext.containsKey(dot.getFirst());

                if (!dotExists) {

                    if (dotValue == 1) {
                        causalContext.put(dotID, dotValue);
                        iterator.remove();
                        flag = true;
                    }

                } else {

                    Integer ccEntryValue = this.causalContext.get(dotID);

                    if (dotValue == ccEntryValue + 1) {

                        this.causalContext.put(dotID, dotValue);
                        iterator.remove();
                        flag = true;

                    } else if (dotValue <= ccEntryValue) iterator.remove();

                }

            }

        } while (flag);

    }

    public void join(DotContext dotContext) {

        if (this == dotContext) return;

        Map<String, Integer> otherCC = dotContext.causalContext;
        Set<Pair<String, Integer>> otherDC = dotContext.dotCloud;

        for (String key : otherCC.keySet()) {

            Integer value = otherCC.get(key);
            boolean dotExists = this.causalContext.containsKey(key);

            if (dotExists) {

                Integer currValue = this.causalContext.get(key);
                this.causalContext.put(key, Math.max(value, currValue));

            } else this.causalContext.put(key, value);

        }

        for (Pair<String, Integer> dot : otherDC) {
            this.insertDot(dot, false);
        }
        this.compact();

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Context\n");

        stringBuilder.append("CC (");
        for (var entry : this.causalContext.entrySet()) {
            stringBuilder.append("[" + entry.getKey() + " : " + entry.getValue().toString() + "]");
        }

        stringBuilder.append(")\nDC (");
        for (Pair<String, Integer> dot : this.dotCloud) {
            stringBuilder.append("[" + dot.getFirst() + " : " + dot.getSecond().toString() + "]");
        }

        stringBuilder.append(")");
        return stringBuilder.toString();
    }

}