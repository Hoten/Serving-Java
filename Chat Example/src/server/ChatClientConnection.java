package server;

import client.ChatServerConnection;
import hoten.serving.ByteArrayReader;
import hoten.serving.ByteArrayWriter;
import hoten.serving.SocketHandler;
import java.io.IOException;
import java.net.Socket;

public class ChatClientConnection extends SocketHandler {

    final public static int SET_USERNAME = 1;
    final public static int CHAT_MESSAGE = 2;
    final public static int LOGOFF = 3;
    final public static int PRIVATE_MESSAGE = 4;
    final private ServingChat server;
    private String username = null;

    public ChatClientConnection(ServingChat server, Socket socket) throws IOException {
        super(socket);
        this.server = server;
    }

    @Override
    protected void handleData(ByteArrayReader reader) throws IOException {
        ByteArrayWriter msg;
        int type = reader.getType();
        switch (type) {
            case SET_USERNAME:
                username = reader.readUTF();
                msg = new ByteArrayWriter();
                msg.setType(ChatServerConnection.PEER_JOIN);
                msg.writeUTF(username);
                server.sendToAllBut(msg, this);
                break;
            case CHAT_MESSAGE:
                msg = new ByteArrayWriter();
                msg.setType(ChatServerConnection.CHAT_MESSAGE);
                msg.writeUTF(username);
                msg.writeUTF(reader.readUTF());
                server.sendToAllBut(msg, this);
                break;
            case LOGOFF:
                close();
                break;
            case PRIVATE_MESSAGE:
                String to = reader.readUTF();
                msg = new ByteArrayWriter();
                msg.setType(ChatServerConnection.PRIVATE_CHAT_MESSAGE);
                msg.writeUTF(username);
                msg.writeUTF(reader.readUTF());
                server.sendToClientWithUsername(msg, to);
                break;
        }
    }

    @Override
    public void close() {
        if (isOpen()) {
            super.close();
            server.removeClient(this);
            if (username != null) {
                ByteArrayWriter msg = new ByteArrayWriter();
                msg.setType(ChatServerConnection.PEER_DISCONNECT);
                msg.writeUTF(username);
                server.sendToAllBut(msg, this);
            }
        }
    }

    public String getUsername() {
        return username;
    }
}
