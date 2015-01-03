package client;

import hoten.serving.message.MessageHandler;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        MessageHandler.loadMessageHandlers(Arrays.asList("client.protocols"));
        Chat chat = new Chat();
        chat.announce("Connecting to server...");
        ConnectionToChatServerHandler serverConnection = new ConnectionToChatServerHandler(chat, new Socket("localhost", 1234));
        serverConnection.start();
    }
}
