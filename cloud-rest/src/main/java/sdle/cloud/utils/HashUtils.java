package sdle.cloud.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class HashUtils {

    private static final Random RANDOM = new Random();

    public static String getHash(String input) {
        MessageDigest md = getMessageDigest();
        byte[] messageDigest = md.digest(input.getBytes());
        return convertToHex(messageDigest);
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

    private static String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }

    public static String getRandomHash() {
        byte[] array = new byte[7]; // length is bounded by 7
        RANDOM.nextBytes(array);
        return getHash(new String(array, StandardCharsets.UTF_8));
    }
}
