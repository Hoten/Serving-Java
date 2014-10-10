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
        ByteArray welcomeMessage = new ByteArray();
        welcomeMessage.writeUTFBytes("Hello! Welcome to the chat. Here is a random number: " + Math.random());
        welcomeMessage.writeUTFBytes("\nAnd this is when the server was started: " + new Date(System.currentTimeMillis()));
        ByteArray.saveAs(new File("clientdata" + File.separator + "welcome.txt"), welcomeMessage);

        ServingSocketExample server = new ServingSocketExample(1234);
        server.startServer();
        System.out.println("Server started.");
    }
}
