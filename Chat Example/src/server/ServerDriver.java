package server;

import hoten.serving.ByteArray;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * ServerDriver.java
 *
 * Starts the chat server.
 *
 * @author Hoten
 */
public class ServerDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 1234;
        String clientDataDirName = "clientdata";
        String localDataDirName = "localdata";
        createRandomWelcomeMessage(clientDataDirName);
        ServingChat server = new ServingChat(port, clientDataDirName, localDataDirName);
        server.startServer();
        System.out.println("Server started.");
    }

    private static void createRandomWelcomeMessage(String clientDataDirName) {
        ByteArray welcomeMessage = new ByteArray();
        welcomeMessage.writeUTFBytes("Hello! Welcome to the chat. Here is a random number: " + Math.random());
        welcomeMessage.writeUTFBytes("\nAnd this is when the server was started: " + new Date(System.currentTimeMillis()));
        ByteArray.saveAs(new File(clientDataDirName, "welcome.txt"), welcomeMessage);
    }
}
