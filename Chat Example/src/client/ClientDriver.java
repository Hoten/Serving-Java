package client;

import hoten.serving.ByteArray;
import java.io.IOException;
import java.net.Socket;

/**
 * ClientDriver.java
 *
 * Connects to server.
 *
 * @author Hoten
 */
public class ClientDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Connecting to server...");
        ByteArray.server = new ServerConnectionExample(new Socket("localhost", 1234));
    }
}
