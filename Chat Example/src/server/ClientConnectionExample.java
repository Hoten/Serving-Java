package server;

import client.ServerConnectionExample;
import hoten.serving.ByteArray;
import hoten.serving.SocketHandler;
import java.io.IOException;
import java.net.Socket;

/**
 * Client.java
 *
 * Extends SocketHandler, and provides the logic for dealing with data from
 * clients.
 *
 * @author Hoten
 */
public class ClientConnectionExample extends SocketHandler {

    final public static int SET_USERNAME = 1;
    final public static int CHAT_MESSAGE = 2;
    final public static int LOGOFF = 3;
    final public static int PRIVATE_MESSAGE = 4;
    final public static int REQUEST_FILE_UPDATES = 5;
    final private ServingSocketExample server;
    private String username = null;

    public ClientConnectionExample(ServingSocketExample server, Socket socket) throws IOException {
        super(socket);
        this.server = server;
        send(server.getClientDataHashes());
        sendWhosOnline();
    }

    @Override
    protected void handleData(ByteArray reader) throws IOException {
        ByteArray msg;
        int type = reader.getType();
        switch (type) {
            case SET_USERNAME:
                username = reader.readUTF();
                msg = new ByteArray();
                msg.setType(ServerConnectionExample.PEER_JOIN);
                msg.writeUTF(username);
                server.sendToAllBut(msg, this);
                break;
            case CHAT_MESSAGE:
                msg = new ByteArray();
                msg.setType(ServerConnectionExample.CHAT_MESSAGE);
                msg.writeUTF(username);
                msg.writeUTF(reader.readUTF());
                server.sendToAllBut(msg, this);
                break;
            case LOGOFF:
                close();
                break;
            case PRIVATE_MESSAGE:
                String to = reader.readUTF();
                msg = new ByteArray();
                msg.setType(ServerConnectionExample.PRIVATE_CHAT_MESSAGE);
                msg.writeUTF(username);
                msg.writeUTF(reader.readUTF());
                server.sendToClientWithUsername(msg, to);
                break;
            case REQUEST_FILE_UPDATES:
                server.getFilesForClient(reader, this, ServerConnectionExample.NEW_FILE, ServerConnectionExample.FINISHED_UPDATING);
                break;
        }
    }

    private void sendWhosOnline() {
        ByteArray msg = new ByteArray();
        msg.setType(ServerConnectionExample.PRINT);
        msg.writeUTF(server.whoIsOnline());
        send(msg);
    }

    @Override
    public void close() {
        if (isOpen()) {
            server.removeClient(this);
            if (username != null) {
                ByteArray msg = new ByteArray();
                msg.setType(ServerConnectionExample.PEER_DISCONNECT);
                msg.writeUTF(username);
                server.sendToAllBut(msg, this);
            }
            super.close();
        }
    }

    public String getUsername() {
        return username;
    }
}
