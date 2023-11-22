package sdle.cloud.utils;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import sdle.cloud.message.CommandEnum;

import java.util.List;
import java.util.Random;

public class ZMQUtils {

    public static ZMQ.Socket newClientSocket(ZContext context) {
        final ZMQ.Socket shoppListClientSocket;
        shoppListClientSocket = context.createSocket(SocketType.DEALER);
        shoppListClientSocket.setIdentity((Thread.currentThread().getName() + new Random().nextInt(1000)).getBytes(ZMQ.CHARSET));
        shoppListClientSocket.setReceiveTimeOut(5000);
        return shoppListClientSocket;
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
