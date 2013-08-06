package hoten.serving;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ServingSocket.java
 *
 * Extend this class to act as a server. See the chat example.
 *
 * @author Hoten
 */
public abstract class ServingSocket extends Thread {

    final private ServerSocket socket;
    final private ScheduledExecutorService heartbeatScheduler;
    final protected CopyOnWriteArrayList<SocketHandler> clients = new CopyOnWriteArrayList();
    private boolean open;

    public ServingSocket(int port) throws IOException {
        super("Serving Socket " + port);
        socket = new ServerSocket(port);

        //start heartbeat
        final int ms = 250;
        heartbeatScheduler = Executors.newScheduledThreadPool(1);
        final ByteArray msg = new ByteArray();
        final Runnable heartbeat = new Runnable() {
            @Override
            public void run() {
                sendToAll(msg);
            }
        };
        heartbeatScheduler.scheduleAtFixedRate(heartbeat, ms, ms, TimeUnit.MILLISECONDS);
    }

    public void sendToAll(ByteArray msg) {
        for (SocketHandler c : clients) {
            c.send(msg);
        }
    }

    public void sendToAllBut(ByteArray msg, SocketHandler client) {
        for (SocketHandler c : clients) {
            if (c != client) {
                c.send(msg);
            }
        }
    }

    @Override
    public void run() {
        open = true;
        while (open) {
            try {
                SocketHandler newClient = makeNewConnection(socket.accept());
                clients.add(newClient);
            } catch (IOException ex) {
                System.out.println("error making new connection: " + ex);
            }
        }
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("error closing server: " + ex);
        }
    }

    public void close() {
        for (SocketHandler c : clients) {
            c.close();
        }
        open = false;
    }

    public void removeClient(SocketHandler client) {
        clients.remove(client);
    }

    protected abstract SocketHandler makeNewConnection(Socket newConnection) throws IOException;
}
