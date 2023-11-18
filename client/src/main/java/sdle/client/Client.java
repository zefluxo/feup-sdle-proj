package sdle.client;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ZContext context = new ZContext()) {
            //  Socket to talk to server
            System.out.println("Connecting to cloud server");
            for (int requestNbr = 1110; requestNbr != 1120; requestNbr++) {
                int finalRequestNbr = requestNbr;
                executor.submit(() -> sendRequest(context, finalRequestNbr));
            }
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
        }
    }

    private static void sendRequest(ZContext context, int requestNbr) {
        // neste exemplo fazendo a conexao dentro do loop apenas vez para verificar o funcionamento do load balance
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.setIdentity((Thread.currentThread().getName() + new Random().nextInt(1000)).getBytes(ZMQ.CHARSET));
        socket.connect("tcp://host.docker.internal:7788");

        //String request = String.format("Hello (client %s)", System.getenv("HOSTNAME"));
        System.out.println("Sending Hello " + requestNbr);

        socket.sendMore("putList");
        socket.send(requestNbr + "list_name");

        String reply = socket.recvStr();
        System.out.printf("Received %s (%s)%n", reply, requestNbr);
    }
}
