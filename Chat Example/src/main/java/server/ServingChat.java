package server;

import hoten.serving.Message;
import hoten.serving.ServingSocket;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.stream.Collectors;

public class ServingChat extends ServingSocket<ConnectionToChatServerHandler> {

    public ServingChat(int port, String clientDataDirName, String localDataDirName) throws IOException {
        super(port, new File(clientDataDirName), localDataDirName);
    }

    public void sendToClientWithUsername(Message message, String username) {
        sendToFirst(message, (c) -> username.equals(c.getUsername()));
    }

    public String whoIsOnline() {
        if (_clients.isEmpty()) {
            return "No users are online at the moment.";
        }
        return "Users currently online:\n" + _clients.stream()
                .map((c) -> c.getUsername())
                .collect(Collectors.joining("\n"));
    }

    @Override
    protected ConnectionToChatServerHandler makeNewConnection(Socket newConnection) throws IOException {
        ConnectionToChatServerHandler clientConnection = new ConnectionToChatServerHandler(this, newConnection);
        return clientConnection;
    }
}
