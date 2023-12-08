package sdle.client.utils;

import jakarta.xml.bind.DatatypeConverter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class HashUtils {

    private static final Random RANDOM = new Random();

    public static String getHash(String input) {
        MessageDigest md = getMessageDigest();
        byte[] messageDigest = md.digest(input.getBytes());
        return DatatypeConverter.printHexBinary(messageDigest);
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
        byte[] array = new byte[7];
        RANDOM.nextBytes(array);
        return getHash(new String(array, StandardCharsets.UTF_8));
    }
}
