package client;

import hoten.serving.ByteArray;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import server.ChatClientConnection;

/**
 * ClientDriver.java
 *
 * Connects to server.
 *
 * @author Hoten
 */
public class ClientDriver {

    private static ChatServerConnection serverConnection;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Connecting to server...");
        serverConnection = new ChatServerConnection(new Socket("localhost", 1234));
        serverConnection.onConnectionSettled(ClientDriver::startChat);
        serverConnection.start();
    }

    private static void startChat() {
        readWelcomeMesssage();

        System.out.print("Enter your username (no spaces): ");
        final Scanner s = new Scanner(System.in);
        String username = s.next();
        s.nextLine();

        ByteArray msg = new ByteArray();
        msg.setType(ChatClientConnection.SET_USERNAME);
        msg.writeUTF(username);
        msg.send();

        Executors.newSingleThreadExecutor().execute(() -> {
            while (processChatInput(s.nextLine()));
            serverConnection.close();
        });
    }

    private static boolean processChatInput(String input) {
        ByteArray msg;
        if (input.startsWith("/")) {
            String[] split = input.split(" ", 2);
            if (split.length != 2) {
                return true;
            }
            String to = split[0].substring(1);
            String whisper = split[1];
            msg = new ByteArray();
            msg.setType(ChatClientConnection.PRIVATE_MESSAGE);
            msg.writeUTF(to);
            msg.writeUTF(whisper);
            msg.send();
        } else if (input.equalsIgnoreCase("quit")) {
            msg = new ByteArray();
            msg.setType(ChatClientConnection.LOGOFF);
            msg.send();
            return false;
        } else {
            msg = new ByteArray();
            msg.setType(ChatClientConnection.CHAT_MESSAGE);
            msg.writeUTF(input);
            msg.send();
        }
        return true;
    }

    private static void readWelcomeMesssage() {
        File f = new File("localdata" + File.separator + "welcome.txt");
        ByteArray ba = ByteArray.readFromFileAndRewind(f);
        System.out.println();
        System.out.println(ba.readUTFBytes(ba.getBytesAvailable()));
        System.out.println();
    }
}
