package client;

import hoten.serving.ConnectionToServerHandler;
import hoten.serving.message.JsonMessageBuilder;
import hoten.serving.message.Message;
import java.io.IOException;
import java.net.Socket;

public class ConnectionToChatServerHandler extends ConnectionToServerHandler {

    private final Chat _chat;

    public ConnectionToChatServerHandler(Chat chat, Socket socket) throws IOException {
        super(socket);
        _chat = chat;
    }

    @Override
    protected void onConnectionSettled() throws IOException {
        _chat.startChat(this);
    }

    public void sendUsername(String username) throws IOException {
        Message message = new JsonMessageBuilder()
                .type("SetUsername")
                .set("username", username)
                .build();
        send(message);
    }

    public void quit() throws IOException {
        Message message = new JsonMessageBuilder()
                .type("LogOff")
                .build();
        send(message);
    }

    public void sendMessage(String msg) throws IOException {
        Message message = new JsonMessageBuilder()
                .type("ChatMessage")
                .compressed(true)
                .set("msg", msg)
                .build();
        send(message);
    }

    public void sendWhisper(String to, String msg) throws IOException {
        Message message = new JsonMessageBuilder()
                .type("Whisper")
                .set("to", to)
                .set("msg", msg)
                .build();
        send(message);
    }

    public Chat getChat() {
        return _chat;
    }
}
