package hoten.serving;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ServingSocket.java
 *
 * Extend this class to act as a server. See the chat example.
 *
 * @author Hoten
 */
public abstract class ServingSocket extends Thread {

    final private ServerSocket socket;
    final protected CopyOnWriteArrayList<SocketHandler> clients = new CopyOnWriteArrayList();
    private boolean open;

    public ServingSocket(int port) throws IOException {
        super("Serving Socket " + port);
        socket = new ServerSocket(port);
        startHeartbeat();
    }

    private void startHeartbeat() {
        new Thread("Heartbeat") {
            @Override
            public void run() {
                ByteArray msg = new ByteArray();
                while (open) {
                    for (SocketHandler c : clients) {
                        c.send(msg);
                    }

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        System.out.println("Thread error: " + ex);
                    }
                }
            }
        }.start();
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
