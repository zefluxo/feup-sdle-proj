package sdle.crdt.utils;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class PairKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        String[] pairs = key.substring(7, key.length() - 1).split("/");
        String first = pairs[0].split("=")[1].trim();
        Integer second = Integer.valueOf(pairs[1].split("=")[1].trim());
        return new Pair<>(first, second);
    }
}