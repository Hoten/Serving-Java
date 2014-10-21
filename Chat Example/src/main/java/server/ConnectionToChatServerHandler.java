package server;

import chat.ClientToServerProtocols;
import chat.ServerToClientProtocols;
import hoten.serving.JsonMessageBuilder;
import hoten.serving.Message;
import hoten.serving.SocketHandler;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class ConnectionToChatServerHandler extends SocketHandler {

    final private ServingChat server;
    private String username = null;
    private final ServerToClientProtocols serverProtocols = new ServerToClientProtocols();
    private final ClientToServerProtocols clientProtocols = new ClientToServerProtocols();

    public ConnectionToChatServerHandler(ServingChat server, Socket socket) throws IOException {
        super(socket, new ClientToServerProtocols());
        this.server = server;
    }

    @Override
    protected void onConnectionSettled() {
        Message msg = new JsonMessageBuilder()
                .protocol(serverProtocols.print)
                .set("msg", server.whoIsOnline())
                .build();
        send(msg);
    }

    @Override
    protected void handleData(int type, Map data) throws IOException {
        Message message;
        //TODO fix. hard to read.
        System.out.println(data);
        if (type == clientProtocols.setUsername.type) {
            username = (String) data.get("username");
            message = new JsonMessageBuilder()
                    .protocol(serverProtocols.peerJoin)
                    .set("username", username)
                    .build();
            server.sendToAllBut(message, this);
        } else if (type == clientProtocols.chatMessage.type) {
            message = new JsonMessageBuilder()
                    .protocol(serverProtocols.chatMessage)
                    .set("from", username)
                    .set("msg", data.get("msg"))
                    .build();
            server.sendToAllBut(message, this);
        } else if (type == clientProtocols.logOff.type) {
            close();
        } else if (type == clientProtocols.privateMessage.type) {
            message = new JsonMessageBuilder()
                    .protocol(serverProtocols.privateMessage)
                    .set("from", username)
                    .set("msg", data.get("msg"))
                    .build();
            server.sendToClientWithUsername(message, (String) data.get("to"));
        }
    }

    @Override
    protected void handleData(int type, DataInputStream data) throws IOException {
    }

    @Override
    public void close() {
        if (isOpen()) {
            super.close();
            server.removeClient(this);
            if (username != null) {
                Message message = new JsonMessageBuilder()
                        .protocol(serverProtocols.peerDisconnect)
                        .set("username", username)
                        .build();
                server.sendToAllBut(message, this);
            }
        }
    }

    public String getUsername() {
        return username;
    }
}
