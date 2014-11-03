package client;

import java.io.IOException;
import java.net.Socket;

public class ClientDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        Chat chat = new Chat();
        chat.announce("Connecting to server...");
        ConnectionToChatServerHandler serverConnection = new ConnectionToChatServerHandler(chat, new Socket("localhost", 1234));
        serverConnection.start();
    }
}
