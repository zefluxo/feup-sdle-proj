package sdle.cloud.utils;

import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ZMQUtils {


    public static void notifyClusterNodesUpdate(ZMQ.Socket clientSocket, Cluster cluster, Node node) {
        System.out.printf("updating the cluster %s%n", cluster.getNodes());
        cluster.getNodes().values().forEach(ip -> {
            if (ip != node.getIp()) {
                ZMQUtils.sendMsg(clientSocket, (String) ip, node.getClusterPort(), CommandEnum.CLUSTER_UPDATE,
                        Collections.singletonList(new JSONObject(cluster.getNodes()).toString()));
            }
        });
    }

    public static ZMQ.Socket newClientSocket(ZContext context) {
        final ZMQ.Socket clientSocket;
        clientSocket = context.createSocket(SocketType.DEALER);
        clientSocket.setIdentity((Thread.currentThread().getName() + new Random().nextInt(1000)).getBytes(ZMQ.CHARSET));
        clientSocket.setReceiveTimeOut(5000);
        return clientSocket;
    }

    public static String sendMsg(ZMQ.Socket socket, String destHost, String destPort, CommandEnum commandEnum, List<String> msg) {
        String destAddr = get0MQAddr(destHost, destPort);
        System.out.printf("Sending command %s:%s to %s%n", commandEnum, msg, destAddr);
        socket.connect(destAddr); // TODO: implementar retentativas em caso de falhas ???

        if (msg.isEmpty()) {
            socket.send(commandEnum.getCmd());
        } else {
            socket.sendMore(commandEnum.getCmd());
            msg.subList(0, msg.size() - 1).forEach(socket::sendMore);
            socket.send(msg.get(msg.size() - 1));
        }
        System.out.printf("Command %s:%s sent to %s%n", commandEnum, msg, destAddr);
        socket.recv(); // identity
//        socket.recvStr(ZMQ.SNDMORE); // ""
        String reply = socket.recvStr();
        System.out.printf("Received %s%n", reply);
        socket.disconnect(destAddr);
        return reply;
    }

    public static void sendReply(ZMQ.Socket serverSocket, List<String> msg, String reply) {
        String clientIdentity = msg.get(0);
        System.out.printf("sendReply: %s (client identity: %s) %n", reply, clientIdentity);
        serverSocket.sendMore(clientIdentity); // sender identity
        serverSocket.sendMore("X");
        serverSocket.send(reply);
    }

    public static String get0MQAddr(String hostname, String port) {
        return String.format("tcp://%s:%s", hostname, port);
    }

}
