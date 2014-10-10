package server;

import client.ServerConnectionExample;
import hoten.serving.ByteArray;
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
public class ServingSocketExample extends ServingSocket<ClientConnectionExample> {

    public ServingSocketExample(int port) throws IOException {
        super(port, 500, new File("clientdata"), "localdata");
    }

    public void sendToClientWithUsername(ByteArray msg, String username) {
        for (ClientConnectionExample c : _clients) {
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

        for (Iterator<ClientConnectionExample> it = _clients.iterator(); it.hasNext();) {
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
    protected ClientConnectionExample makeNewConnection(Socket newConnection) throws IOException {        
        ClientConnectionExample clientConnection = new ClientConnectionExample(this, newConnection);
        clientConnection.onConnectionSettled(() -> {
            ByteArray msg = new ByteArray();
            msg.setType(ServerConnectionExample.PRINT);
            msg.writeUTF(whoIsOnline());
            clientConnection.send(msg);
        });
        return clientConnection;
    }
}
