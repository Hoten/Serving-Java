package client;

import hoten.serving.ByteArray;
import server.ClientConnectionExample;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * ClientDriver.java Function Date Aug 5, 2013
 *
 * @author Connor
 */
public class ClientDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        final ServerConnectionExample socket;
        final Scanner s = new Scanner(System.in);
        final String username;

        System.out.print("Enter your username (no spaces): ");
        username = s.next();
        s.nextLine();

        System.out.println("Connecting to server...");

        ByteArray.server = socket = new ServerConnectionExample(new Socket("localhost", 1234));

        System.out.println("Connection made.");

        ByteArray msg = new ByteArray();
        msg.setType(ClientConnectionExample.SET_USERNAME);
        msg.writeUTF(username);
        msg.send();

        while (true) {
            String line = s.nextLine();
            if (line.startsWith("/")) {
                String[] split = line.split(" ", 2);
                if (split.length != 2) {
                    continue;
                }
                String to = split[0].substring(1);
                String whisper = split[1];
                msg = new ByteArray();
                msg.setType(ClientConnectionExample.PRIVATE_MESSAGE);
                msg.writeUTF(to);
                msg.writeUTF(whisper);
                msg.send();
            } else if (line.equalsIgnoreCase("quit")) {
                msg = new ByteArray();
                msg.setType(ClientConnectionExample.LOGOFF);
                msg.send();
                break;
            } else {
                msg = new ByteArray();
                msg.setType(ClientConnectionExample.CHAT_MESSAGE);
                msg.writeUTF(line);
                msg.send();
            }
        }

        socket.close();
    }
}
