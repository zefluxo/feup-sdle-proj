package sdle.crdt.utils;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class PairKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
//        System.out.println("Deserializing: " + key);
        String[] pairs = key.substring(7, key.length() - 1).split("/");
//        Arrays.stream(pairs).forEach(System.out::println);
        String first = pairs[0].split("=")[1].trim();
        String second = pairs[1].split("=")[1].trim();
        return new Pair<>(first, second);
    }
}