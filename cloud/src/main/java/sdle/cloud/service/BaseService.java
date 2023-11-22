package sdle.cloud.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import sdle.cloud.cluster.Cluster;
import sdle.cloud.cluster.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RequiredArgsConstructor
public abstract class BaseService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(50);
    protected final ZContext context = new ZContext();

    @Getter(AccessLevel.PROTECTED)
    private final Node node;
    @Getter(AccessLevel.PROTECTED)
    private final Cluster cluster;
    @Getter(AccessLevel.PROTECTED)
    private ZMQ.Socket socket;

    public void init() {
        //System.out.println(node);
        executor.submit(this::initServer);
    }

    private void initServer() {
        socket = context.createSocket(SocketType.ROUTER);
        //socket.setRouterMandatory(true);
        String addr = String.format("tcp://*:%s", getServicePort());
        socket.bind(addr);
        System.out.printf("server up: %s%n", addr);
        while (!Thread.currentThread().isInterrupted()) {
            List<String> msg = new ArrayList<>();
            do {
                msg.add(socket.recvStr());
            } while (socket.hasReceiveMore());
            System.out.printf("[%s,%s] receive: %s%n", node.getIp(), getServicePort(), msg);
            executor.execute(() -> processMsg(msg));
        }
    }

//    abstract protected void onInterrupt();

    abstract protected String getServicePort();

    protected abstract void processMsg(List<String> msg);
}
