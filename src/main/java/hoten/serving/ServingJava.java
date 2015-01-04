package hoten.serving;

import hoten.serving.message.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServingJava<S extends SocketHandler> {

    final private ExecutorService exec = Executors.newCachedThreadPool();
    final private ServerSocket _socket;
    final protected List<S> _clients = new CopyOnWriteArrayList();

    public ServingJava(int port) throws IOException {
        _socket = new ServerSocket(port);
    }

    protected abstract S makeNewConnection(Socket newConnection) throws IOException; // :(

    protected abstract void setupNewClient(S newClient) throws IOException;

    protected abstract void onClientClose(S client) throws IOException;

    public void startServer() {
        exec.execute(() -> {
            while (true) {
                try {
                    final S newClient = makeNewConnection(_socket.accept());
                    exec.execute(() -> {
                        try {
                            setupNewClient(newClient);
                            newClient.start(() -> {
                                _clients.add(newClient);
                            }, newClient);
                        } catch (InstantiationException | IllegalAccessException ex) {
                            Logger.getLogger(ServingJava.class.getName()).log(Level.SEVERE, null, ex);
                            removeClient(newClient);
                        } catch (IOException ex) {
                            removeClient(newClient);
                        }
                    });
                } catch (IOException ex) {
                    Logger.getLogger(ServingJava.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void close() {
        exec.shutdown();
        _clients.stream().forEach(c -> {
            c.close();
        });
    }

    private void removeClient(S client) {
        _clients.remove(client);
        client.close();
        try {
            onClientClose(client);
        } catch (IOException ex) {
            Logger.getLogger(ServingJava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendTo(Message message, S client) {
        try {
            client.send(message);
        } catch (IOException ex) {
            removeClient(client);
        }
    }

    public void sendTo(Message message, Predicate<S> selector) {
        _clients.stream().filter(selector).forEach((c) -> {
            sendTo(message, c);
        });
    }

    public void sendToFirst(Message message, Predicate<S> selector) {
        _clients.stream().filter(selector).findFirst().ifPresent((c) -> {
            sendTo(message, c);
        });
    }

    public void sendToAll(Message message) {
        sendTo(message, c -> true);
    }

    public void sendToAllBut(Message message, SocketHandler client) {
        sendTo(message, c -> c != client);
    }
}
