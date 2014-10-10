package server;

import client.ChatServerConnection;
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
public class ServingChat extends ServingSocket<ChatClientConnection> {

    public ServingChat(int port) throws IOException {
        super(port, 500, new File("clientdata"), "localdata");
    }

    public void sendToClientWithUsername(ByteArray msg, String username) {
        for (ChatClientConnection c : _clients) {
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

        for (Iterator<ChatClientConnection> it = _clients.iterator(); it.hasNext();) {
            ChatClientConnection c = (ChatClientConnection) it.next();
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
    protected ChatClientConnection makeNewConnection(Socket newConnection) throws IOException {        
        ChatClientConnection clientConnection = new ChatClientConnection(this, newConnection);
        clientConnection.onConnectionSettled(() -> {
            ByteArray msg = new ByteArray();
            msg.setType(ChatServerConnection.PRINT);
            msg.writeUTF(whoIsOnline());
            clientConnection.send(msg);
        });
        return clientConnection;
    }
}
