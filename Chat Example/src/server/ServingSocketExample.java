package server;

import client.ServerConnectionExample;
import hoten.serving.ByteArray;
import hoten.serving.ServingSocket;
import hoten.serving.SocketHandler;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

/**
 * ServingSocketExample.java
 *
 * Passes new chat connections (ClientConnectionExample) to the server code, and
 * provides additional server infrastructure.
 *
 * @author Hoten
 */
public class ServingSocketExample extends ServingSocket {

    public ServingSocketExample(int port) throws IOException {
        super(port, 500, new File("clientdata"));
        getClientDataHashes().setType(ServerConnectionExample.HASHES);
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

    public String whoIsOnline() {
        StringBuilder builder = new StringBuilder();
        builder.append("Users currently online:\n");
        if (clients.isEmpty()) {
            builder.append("none (yet!)");
        } else {
            for (Iterator<SocketHandler> it = clients.iterator(); it.hasNext();) {
                ClientConnectionExample c = (ClientConnectionExample) it.next();
                builder.append(c.getUsername());
                if (it.hasNext()) {
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    @Override
    protected SocketHandler makeNewConnection(Socket newConnection) throws IOException {
        return new ClientConnectionExample(this, newConnection);
    }
}
