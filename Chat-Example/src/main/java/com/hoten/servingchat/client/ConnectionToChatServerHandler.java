package com.hoten.servingchat.client;

import com.hoten.servingjava.SocketHandler;
import com.hoten.servingjava.SocketHandlerImpl;
import com.hoten.servingjava.filetransferring.FileTransferringSocketReciever;
import com.hoten.servingjava.message.JsonMessageBuilder;
import com.hoten.servingjava.message.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class ConnectionToChatServerHandler implements SocketHandler {

    private final FileTransferringSocketReciever _socketHandler;
    private final Chat _chat;

    public ConnectionToChatServerHandler(Socket socket, Chat chat) throws IOException {
        _socketHandler = new FileTransferringSocketReciever(new SocketHandlerImpl(socket));
        _chat = chat;
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
                /*.compressed(true)*/ // :(
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

    @Override
    public void start(Runnable onConnectionSettled, SocketHandler topLevelSocketHandler) throws IOException, InstantiationException, IllegalAccessException {
        _socketHandler.start(onConnectionSettled, topLevelSocketHandler);
    }

    @Override
    public void send(Message message) throws IOException {
        _socketHandler.send(message);
    }

    @Override
    public void close() {
        _socketHandler.close();
    }

    @Override
    public DataOutputStream getOutputStream() {
        return _socketHandler.getOutputStream();
    }

    @Override
    public DataInputStream getInputStream() {
        return _socketHandler.getInputStream();
    }

    public File getLocalDataFolder() {
        return _socketHandler.getLocalDataFolder();
    }

    public Chat getChat() {
        return _chat;
    }
}
