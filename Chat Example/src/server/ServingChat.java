package server;

import client.ChatServerConnection;
import hoten.serving.ByteArrayWriter;
import hoten.serving.ServingSocket;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.stream.Collectors;

public class ServingChat extends ServingSocket<ChatClientConnection> {

    public ServingChat(int port, String clientDataDirName, String localDataDirName) throws IOException {
        super(port, 500, new File(clientDataDirName), localDataDirName);
    }

    public void sendToClientWithUsername(ByteArrayWriter msg, String username) {
        sendToFirst(msg, (c) -> username.equals(c.getUsername()));
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
    protected ChatClientConnection makeNewConnection(Socket newConnection) throws IOException {
        ChatClientConnection clientConnection = new ChatClientConnection(this, newConnection);
        clientConnection.onConnectionSettled(() -> {
            ByteArrayWriter msg = new ByteArrayWriter();
            msg.setType(ChatServerConnection.PRINT);
            msg.writeUTF(whoIsOnline());
            clientConnection.send(msg);
        });
        return clientConnection;
    }
}
