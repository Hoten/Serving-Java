package server;

import hoten.serving.SocketHandler;
import hoten.serving.SocketHandlerImpl;
import hoten.serving.message.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionToChatClientHandler implements SocketHandler {

    private final SocketHandler _socketHandler;
    private final ServingChat _servingChat;
    private String _username;

    public ConnectionToChatClientHandler(Socket socket, ServingChat servingChat) throws IOException {
        _socketHandler = new SocketHandlerImpl(socket);
        _servingChat = servingChat;
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

    public ServingChat getServingChat() {
        return _servingChat;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }
}
