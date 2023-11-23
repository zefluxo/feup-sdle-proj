package sdle.client;

import lombok.SneakyThrows;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String[] args) {
        Arrays.stream(args).forEach(System.out::println);
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ZContext context = new ZContext()) {
            //  Socket to talk to server
            if (args.length < 1) {
                sendPredefined(executor, context);
            } else {
                List<String> msgArgs = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
                sendRequest(context, args[0], args[1], msgArgs, 0);
            }
        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
        }
    }

    private static void sendPredefined(ExecutorService executor, ZContext context) throws InterruptedException {
        System.out.println("Connecting to cloud server");
        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
            int finalRequestNbr = requestNbr;
            executor.submit(() -> sendRequest(context, "host.docker.internal", "putList", Collections.emptyList(), finalRequestNbr));
        }
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @SneakyThrows
    private static void sendRequest(ZContext context, String dest, String cmd, List<String> msgArgs, int requestNbr) {
        // neste exemplo fazendo a conexao dentro do loop apenas vez para verificar o funcionamento do load balance
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.setReceiveTimeOut(5000);
//        String identity = Thread.currentThread().getName() + new Random().nextInt();
        String identity = "ClientId" + new Random().nextInt();
        socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
        //socket.connect("tcp://host.docker.internal:7788");
        String url = String.format("tcp://%s:7788", dest);
        System.out.printf("identity: %s, url: %s%n", identity, url);
        socket.connect(url);

        //String request = String.format("Hello (client %s)", System.getenv("HOSTNAME"));
        System.out.printf("Sending %s with args <%s> (%s) %n", cmd, msgArgs, requestNbr);

        if (msgArgs.isEmpty()) {
            socket.send(cmd);
        } else {
            socket.sendMore(cmd);
            msgArgs.subList(0, msgArgs.size() - 1).forEach(socket::sendMore);
            socket.send(msgArgs.get(msgArgs.size() - 1));
        }

        socket.recvStr(); //""
//        socket.recvStr(ZMQ.SNDMORE); // ""
        String reply = socket.recvStr();
        System.out.printf("Received %s (%s)%n", reply, requestNbr);
        socket.disconnect(url);
        socket.close();
    }
}
