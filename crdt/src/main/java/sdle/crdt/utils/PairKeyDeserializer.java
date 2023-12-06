package sdle.crdt.utils;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class PairKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        //System.out.println(key);
        return new Pair<>();
    }
}