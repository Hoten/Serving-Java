package com.hoten.servingchat.server;

import com.hoten.servingjava.message.Message;
import com.hoten.servingjava.filetransferring.ServingFileTransferring;
import com.hoten.servingjava.message.JsonMessageBuilder;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.stream.Collectors;

public class ServingChat extends ServingFileTransferring<ConnectionToChatClientHandler> {

    public ServingChat(int port, String clientDataDirName, String localDataDirName) throws IOException {
        super(port, new File(clientDataDirName), localDataDirName);
    }

    @Override
    public void setupNewClient(ConnectionToChatClientHandler newClient) throws IOException {
        super.setupNewClient(newClient);
        Message msg = new JsonMessageBuilder()
                .type("Print")
                .set("msg", whoIsOnline())
                .build();
        newClient.send(msg);
    }

    @Override
    protected void onClientClose(ConnectionToChatClientHandler client) {
        if (client.getUsername() != null) {
            System.out.println(client.getUsername() + " has left.");
            Message message = new JsonMessageBuilder()
                    .type("PeerDisconnect")
                    .set("username", client.getUsername())
                    .build();
            sendToAll(message);
        }
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
    protected ConnectionToChatClientHandler makeNewConnection(Socket newConnection) throws IOException {
        return new ConnectionToChatClientHandler(newConnection, this);
    }
}
