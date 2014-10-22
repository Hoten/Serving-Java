package client;

import chat.ChatProtocols;
import chat.ChatProtocols.Clientbound;
import hoten.serving.ConnectionToServerHandler;
import hoten.serving.JsonMessageBuilder;
import hoten.serving.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import static chat.ChatProtocols.Serverbound.*;
import com.google.gson.JsonObject;

public class ConnectionToChatServerHandler extends ConnectionToServerHandler {

    private final Chat _chat;

    public ConnectionToChatServerHandler(Chat chat, Socket socket) throws IOException {
        super(socket, new ChatProtocols());
        _chat = chat;
    }

    public void quit() {
        Message message = new JsonMessageBuilder()
                .protocol(outbound(LogOff))
                .build();
        send(message);
    }

    public void sendMessage(String msg) {
        Message message = new JsonMessageBuilder()
                .protocol(outbound(ChatMessage))
                .set("msg", msg)
                .build();
        send(message);
    }

    public void sendWhisper(String to, String msg) {
        Message message = new JsonMessageBuilder()
                .protocol(outbound(PrivateMessage))
                .set("to", to)
                .set("msg", msg)
                .build();
        send(message);
    }

    @Override
    protected void onConnectionSettled() {
        _chat.startChat(this);
    }

    @Override
    protected void handleData(int type, JsonObject data) throws IOException {
        switch (Clientbound.values()[type]) {
            case PeerJoin:
                _chat.announceNewUser(data.get("username").getAsString());
                break;
            case ChatMessage:
                _chat.global(data.get("from").getAsString(), data.get("msg").getAsString());
                break;
            case PeerDisconnect:
                _chat.announceDisconnect(data.get("username").getAsString());
                break;
            case PrivateMessage:
                _chat.whisper(data.get("from").getAsString(), data.get("msg").getAsString());
                break;
            case Print:
                _chat.announce(data.get("msg").getAsString());
                break;
        }
    }

    @Override
    protected void handleData(int type, DataInputStream data) {
    }

    void sendUsername(String username) {
        Message message = new JsonMessageBuilder()
                .protocol(outbound(SetUsername))
                .set("username", username)
                .build();
        send(message);
    }
}
