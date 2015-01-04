package client;

import hoten.serving.message.MessageHandler;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientDriver {

    public static void main(String[] args) throws Exception {
        MessageHandler.loadMessageHandlers(Arrays.asList("client.protocols"));
        Chat chat = new Chat();
        chat.announce("Connecting to server...");
        Socket socket = new Socket("localhost", 1234);
        ConnectionToChatServerHandler serverConnection = new ConnectionToChatServerHandler(socket, chat);
        serverConnection.start(() -> {
            try {
                chat.start(serverConnection);
            } catch (IOException ex) {
                Logger.getLogger(ClientDriver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, serverConnection);
    }
}
