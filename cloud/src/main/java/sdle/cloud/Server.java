package sdle.cloud;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Server {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5555");
            String hostname = System.getenv("HOSTNAME");
            System.out.printf("Server up (hostname=%s), using port 5555%n", hostname);
            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                System.out.println(
                        "Received " + ": [" + new String(reply, ZMQ.CHARSET) + "]"
                );
                String response = String.format("world (from  %s)!", hostname);
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
            }
        }
    }
}
