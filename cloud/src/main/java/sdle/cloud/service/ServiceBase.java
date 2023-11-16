package sdle.cloud.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RequiredArgsConstructor
public abstract class ServiceBase {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Getter(AccessLevel.PROTECTED)
    private final Node node;

    @Getter(AccessLevel.PROTECTED)
    private ZMQ.Socket socket;

    public void init() {
        //System.out.println(node);
        try (ZContext context = new ZContext()) {
            socket = context.createSocket(SocketType.ROUTER);
            socket.bind(get0MQAddr());
            while (!Thread.currentThread().isInterrupted()) {
                List<String> msg = new ArrayList<>();
                do {
                    msg.add(socket.recvStr());
                } while (socket.hasReceiveMore());
                executor.execute(() -> processMsg(msg));
            }
        }
    }

    protected abstract String get0MQAddr();

    protected abstract void processMsg(List<String> msg);

}
