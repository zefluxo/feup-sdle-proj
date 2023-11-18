package sdle.cloud.utils;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class HashUtils {

    private static final Random RANDOM = new Random();

    public static String getHash(String src) {
        // TODO: avaliar necessaidade de outro algoritmos (MD5, SHA-1, SHA-256, etc)
        return String.valueOf(src.hashCode());
    }

    public static String getRandomHash() {
        byte[] array = new byte[7]; // length is bounded by 7
        RANDOM.nextBytes(array);
        return getHash(new String(array, StandardCharsets.UTF_8));
    }
}
