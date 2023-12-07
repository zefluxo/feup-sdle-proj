package sdle.cloud.utils;

import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashUtilsTest {

    @Test
    void getHashTest() {
        assertEquals("EF76CCABC36D33AECAB8A4A3B3CA8A8D", HashUtils.getHash("SDLE"));
    }

    @Test
    void getRandomHash() {
        // not too effective...
        String hash1 = HashUtils.getRandomHash();
        assertEquals(32, hash1.length());
        String hash2 = HashUtils.getRandomHash();
        assertEquals(32, hash2.length());
        assertNotEquals(hash1, hash2);
    }

    @Test
    void getNextHashId() {
        TreeMap<String, String> nodeHashes = new TreeMap<>();
        nodeHashes.put("001_nodeHash", "node1_ip");
        nodeHashes.put("011_nodeHash", "node1_ip");
        nodeHashes.put("021_nodeHash", "node1_ip");
        nodeHashes.put("031_nodeHash", "node1_ip");

//        assertNull(HashUtils.getNextHashId("000_list_hash", nodeHashes));

        assertEquals("001_nodeHash", HashUtils.getNextHashId("031_nodeHash", nodeHashes));
        assertEquals("011_nodeHash", HashUtils.getNextHashId("001_nodeHash", nodeHashes));
        assertEquals("021_nodeHash", HashUtils.getNextHashId("011_nodeHash", nodeHashes));
        assertEquals("031_nodeHash", HashUtils.getNextHashId("021_nodeHash", nodeHashes));


        assertEquals("001_nodeHash", HashUtils.getNextHashId("000_list_hash", nodeHashes));
        assertEquals("011_nodeHash", HashUtils.getNextHashId("002_list_hash", nodeHashes));
        assertEquals("021_nodeHash", HashUtils.getNextHashId("014_list_hash", nodeHashes));
        assertEquals("021_nodeHash", HashUtils.getNextHashId("020_list_hash", nodeHashes));
        assertEquals("031_nodeHash", HashUtils.getNextHashId("028_list_hash", nodeHashes));
        assertEquals("001_nodeHash", HashUtils.getNextHashId("099_list_hash", nodeHashes));
    }


}