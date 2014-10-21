package client;

import java.io.IOException;
import java.net.Socket;

public class ClientDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Connecting to server...");
        Chat chat = new Chat();
        ConnectionToChatServerHandler serverConnection = new ConnectionToChatServerHandler(chat, new Socket("localhost", 1234));
        serverConnection.start();
    }
}
