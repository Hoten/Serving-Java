package server;

import hoten.serving.ByteArray;
import hoten.serving.ClientConnectionHandler;
import hoten.serving.ServingSocket;
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
        super(port, 500, new File("clientdata"), "localdata");
    }

    public void sendToClientWithUsername(ByteArray msg, String username) {
        for (ClientConnectionHandler _c : clients) {
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
        boolean any = false;

        for (Iterator<ClientConnectionHandler> it = clients.iterator(); it.hasNext();) {
            ClientConnectionExample c = (ClientConnectionExample) it.next();
            String un = c.getUsername();
            if (un != null) {
                any = true;
                builder.append(un);
            }
            if (it.hasNext()) {
                builder.append("\n");
            }
        }

        if (!any) {
            builder.append("none (yet!)");
        }

        return builder.toString();
    }

    @Override
    protected ClientConnectionHandler makeNewConnection(Socket newConnection) throws IOException {
        return new ClientConnectionExample(this, newConnection);
    }
}
