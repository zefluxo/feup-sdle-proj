package sdle.cloud.cluster;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Node {

    final String id;
    final String hostname;
    final String ip;
    final String port;
    final String clusterPort;
    final String bootstrap;
    final List<String> bootstrapList = new ArrayList<>();
    String hashId;

    @Synchronized
    public List<String> getBootstrapList() {
        if (bootstrapList.isEmpty()) {
            Collections.addAll(bootstrapList, bootstrap.split(","));
        }
        return bootstrapList;
    }

    @Synchronized
    public String getHashId() {
        if (hashId == null) {
            hashId = String.valueOf(id.hashCode()); // TODO: avaliar necessidade de uma  funcao propria de hash
        }
        return hashId;
    }
}
