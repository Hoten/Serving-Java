package server;

import hoten.serving.message.JsonMessageBuilder;
import hoten.serving.message.Message;
import hoten.serving.SocketHandler;
import java.io.IOException;
import java.net.Socket;

public class ConnectionToChatClientHandler extends SocketHandler {

    final public ServingChat server; // :(
    private String _username;

    public ConnectionToChatClientHandler(ServingChat server, Socket socket) throws IOException {
        super(socket);
        this.server = server;
    }

    @Override
    protected void onConnectionSettled() throws IOException {
        Message msg = new JsonMessageBuilder()
                .type("Print")
                .set("msg", server.whoIsOnline())
                .build();
        send(msg);
    }

    @Override
    protected void close() {
        super.close();
        if (_username != null) {
            Message message = new JsonMessageBuilder()
                    .type("PeerDisconnect")
                    .set("username", _username)
                    .build();
            server.sendToAllBut(message, this);
        }
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }
}
