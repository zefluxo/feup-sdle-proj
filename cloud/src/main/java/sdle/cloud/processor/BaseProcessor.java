package sdle.cloud.processor;

import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;
import sdle.cloud.message.CommandEnum;

import java.util.Collections;
import java.util.List;

public abstract class BaseProcessor {
    public static final String REPLY_NOT_FOUND = "<not found>";
    public static final String REPLY_OK = "OK";

    abstract public String process(ZMQ.Socket serverSocket, ZMQ.Socket clientSocket, List<String> msg, Cluster cluster, Node node);

    public String sendMsg(ZMQ.Socket socket, String destHost, String destPort, CommandEnum commandEnum) {
        return sendMsg(socket, destHost, destPort, commandEnum, Collections.emptyList());
    }

    public String sendMsg(ZMQ.Socket socket, String destHost, String destPort, CommandEnum commandEnum, List<String> msg) {
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
        String reply = socket.recvStr();
        System.out.printf("Received %s%n", reply);
        socket.disconnect(destAddr);
        return reply;
    }

    protected String get0MQAddr(String hostname, String port) {
        return String.format("tcp://%s:%s", hostname, port);
    }

    protected void sendReply(ZMQ.Socket socket, List<String> msg, Cluster cluster, Node node, String reply) {
        System.out.println(reply);
        socket.sendMore(msg.get(0)); // sender identity
        socket.sendMore("");
        socket.send(reply);
    }
}
