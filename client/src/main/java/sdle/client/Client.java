package sdle.client;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Client {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            //  Socket to talk to server
            System.out.println("Connecting to cloud server");

            for (int requestNbr = 0; requestNbr != 30; requestNbr++) {
                // neste exemplo fazendo a conexao dentro do loop apenas vez para verificar o funcionalmento do load balance
                ZMQ.Socket socket = context.createSocket(SocketType.REQ);
                socket.connect("tcp://host.docker.internal:7777");

                String request = String.format("Hello (client %s)", System.getenv("HOSTNAME"));
                System.out.println("Sending Hello " + requestNbr);
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);
                System.out.printf("Received %s (%s)%n", new String(reply, ZMQ.CHARSET), requestNbr);
            }
        }
    }
}
