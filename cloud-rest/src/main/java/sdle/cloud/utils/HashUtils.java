package sdle.cloud.utils;

import jakarta.xml.bind.DatatypeConverter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class HashUtils {

    private static final Random RANDOM = new Random();

    public static String getHash(String input) {
        MessageDigest md = getMessageDigest();
        byte[] messageDigest = md.digest(input.getBytes());
        return DatatypeConverter.printHexBinary(messageDigest);
    }

    public static String getNextHashId(String newHashId, TreeMap<String, String> nodesHashes) {
        if (nodesHashes.isEmpty()) return null;
        Map.Entry<String, String> nextNodeEntry = nodesHashes.higherEntry(newHashId);
        if (nextNodeEntry != null) {
            return nextNodeEntry.getKey();
        }
        return nodesHashes.firstEntry().getKey();
    }

    private static MessageDigest getMessageDigest() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return md;
    }

    public static String getRandomHash() {
        byte[] array = new byte[7]; // length is bounded by 7
        RANDOM.nextBytes(array);
        return getHash(new String(array, StandardCharsets.UTF_8));
    }
}
