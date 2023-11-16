package sdle.cloud.cluster;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Node {

    final String id;
    String hashId;
    final String hostname;
    final String ip;
    final String port;
    final String clusterPort;
    final String bootstrap;
}
