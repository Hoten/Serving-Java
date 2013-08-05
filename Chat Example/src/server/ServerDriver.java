package server;

import java.io.IOException;

/**
 * ServerDriver.java
 *
 * Starts the chat server.
 *
 * @author Hoten
 */
public class ServerDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        ServingSocketExample server = new ServingSocketExample(1234);
        server.start();
        System.out.println("Server started.");
    }
}
