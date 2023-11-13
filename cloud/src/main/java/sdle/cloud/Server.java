package sdle.cloud;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Server {
    public static void main(String[] args) throws UnknownHostException {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:7777");
            String hostname = Inet4Address.getLocalHost().getHostName();
            String hostIp = Inet4Address.getLocalHost().getHostAddress();
            System.out.printf("Server up (hostname=%s, ip=%s), using port 7777%n", hostname, hostIp);
            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                System.out.println(
                        "Received " + ": [" + new String(reply, ZMQ.CHARSET) + "]"
                );
                String response = String.format("world (from  %s %s)!", hostname, hostIp);
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
            }
        }
    }
}
