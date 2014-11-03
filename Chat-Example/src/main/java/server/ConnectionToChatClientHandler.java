package server;

import chat.ChatProtocols;
import chat.ChatProtocols.Serverbound;
import hoten.serving.message.JsonMessageBuilder;
import hoten.serving.message.Message;
import hoten.serving.message.Protocols;
import hoten.serving.SocketHandler;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import static chat.ChatProtocols.Clientbound.*;
import com.google.gson.JsonObject;

public class ConnectionToChatClientHandler extends SocketHandler {

    final private ServingChat server;
    private String username;

    public ConnectionToChatClientHandler(ServingChat server, Socket socket) throws IOException {
        super(socket, new ChatProtocols(), Protocols.BoundDest.CLIENT);
        this.server = server;
    }

    @Override
    protected void onConnectionSettled() throws IOException {
        Message msg = new JsonMessageBuilder()
                .protocol(outbound(Print))
                .set("msg", server.whoIsOnline())
                .build();
        send(msg);
    }

    @Override
    protected void handleData(int type, JsonObject data) throws IOException {
        Message message;
        switch (Serverbound.values()[type]) {
            case SetUsername:
                username = data.get("username").getAsString();
                System.out.println(username + " has joined.");
                message = new JsonMessageBuilder()
                        .protocol(outbound(PeerJoin))
                        .set("username", username)
                        .build();
                server.sendToAllBut(message, this);
                break;
            case ChatMessage:
                message = new JsonMessageBuilder()
                        .protocol(outbound(ChatMessage))
                        .set("from", username)
                        .set("msg", data.get("msg").getAsString())
                        .build();
                server.sendToAllBut(message, this);
                break;
            case LogOff:
                closeIfOpen();
                break;
            case PrivateMessage:
                message = new JsonMessageBuilder()
                        .protocol(outbound(PrivateMessage))
                        .set("from", username)
                        .set("msg", data.get("msg").getAsString())
                        .build();
                server.sendToClientWithUsername(message, data.get("to").getAsString());
                break;
        }
    }

    @Override
    protected void handleData(int type, DataInputStream data) throws IOException {
    }

    @Override
    protected void close() {
        super.close();
        if (username != null) {
            Message message = new JsonMessageBuilder()
                    .protocol(outbound(PeerDisconnect))
                    .set("username", username)
                    .build();
            server.sendToAllBut(message, this);
        }
    }

    public String getUsername() {
        return username;
    }
}
