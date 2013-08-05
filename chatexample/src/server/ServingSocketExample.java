package server;

import hoten.serving.ByteArray;
import hoten.serving.ServingSocket;
import hoten.serving.SocketHandler;
import java.io.IOException;
import java.net.Socket;

/**
 * ServingSocketExample.java Function Date Aug 5, 2013
 *
 * @author Connor
 */
public class ServingSocketExample extends ServingSocket {

    public ServingSocketExample(int port) throws IOException {
        super(port);
    }

    public void sendToClientWithUsername(ByteArray msg, String username) {
        for (SocketHandler _c : clients) {
            ClientConnectionExample c = (ClientConnectionExample) _c;
            if (username.equals(c.getUsername())) {
                c.send(msg);
                break;
            }
        }
    }

    @Override
    protected SocketHandler makeNewConnection(Socket newConnection) throws IOException {
        return new ClientConnectionExample(this, newConnection);
    }
}
