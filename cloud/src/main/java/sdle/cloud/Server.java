package sdle.cloud;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import sdle.cloud.message.MessageEnum;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    Executor executor;
    public static void main(String[] args) throws UnknownHostException {
        Properties prop = System.getProperties();
        String nodeId = prop.getProperty("sdle.nodeId", "node"+new Random().nextInt());
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.ROUTER);
            socket.bind("tcp://*:7777");

            System.out.printf("Server up (hostname=%s, ip=%s), using port 7777%n",
                    Inet4Address.getLocalHost().getHostName(),
                    Inet4Address.getLocalHost().getHostAddress());

            while (!Thread.currentThread().isInterrupted()) {
                List<String> msg = new ArrayList<>();
                do {
                    msg.add(socket.recvStr());
                } while (socket.hasReceiveMore());
                System.out.println("antes " + msg);
                executor.execute(() -> process(nodeId, socket, msg));
                System.out.println("depois " + msg);
//                String response = String.format("world (from  %s %s)!", hostname, hostIp);
            }
        }
    }

    private static void process(String nodeId, ZMQ.Socket socket, List<String> msg) {
        System.out.println(Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        msg.forEach(System.out::println);
        MessageEnum messageEnum = MessageEnum.getMessage(msg.get(2));
        String response;
        switch (messageEnum) {
            case PUT_LIST -> {
                response = String.format("adicionada nova lista %s", msg.get(3));
            }
            case GET_LIST -> {
                response = String.format("retornando a lista %s", msg.get(3));
            }
            case DELETE_LIST -> {
                response = String.format("apagando a lista %s", msg.get(3));
            }
            case PUT_ITEM -> {
                response = String.format("adicionado item %s à lista %s", msg.get(4), msg.get(3));
            }
            case GET_ITEM -> {
                response = String.format("retornando item %s da lista %s", msg.get(4), msg.get(3));
            }
            case DELETE_ITEM -> {
                response = String.format("apagando o item %s da lista %s", msg.get(4), msg.get(3));
            }
            default -> {
                response = String.format("command [%s] not implemented", msg.get(2));
            }
        }
        System.out.println(response);
        socket.sendMore(msg.get(0));
        socket.sendMore("");
        socket.send(response);
    }
}