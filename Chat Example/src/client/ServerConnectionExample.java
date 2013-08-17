package client;

import hoten.serving.ByteArray;
import hoten.serving.ServerConnectionHandler;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import server.ClientConnectionExample;

/**
 * Client.java
 *
 * Extends ServerConnectionHandler, and provides the logic for dealing with data
 * from the server.
 *
 * @author Hoten
 */
public class ServerConnectionExample extends ServerConnectionHandler {

    final public static int PEER_JOIN = 1;
    final public static int CHAT_MESSAGE = 2;
    final public static int PEER_DISCONNECT = 3;
    final public static int PRIVATE_CHAT_MESSAGE = 4;
    final public static int PRINT = 5;

    public ServerConnectionExample(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    protected void onConnectionSettled() throws IOException {
        System.out.println("Connection made.");

        readWelcomeMesssage();

        System.out.print("Enter your username (no spaces): ");
        final Scanner s = new Scanner(System.in);
        String username = s.next();
        s.nextLine();

        ByteArray msg = new ByteArray();
        msg.setType(ClientConnectionExample.SET_USERNAME);
        msg.writeUTF(username);
        msg.send();

        new Thread("chat input") {
            @Override
            public void run() {
                ByteArray msg;
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
                close();
            }
        }.start();
    }

    @Override
    protected void handleData(ByteArray reader) throws IOException {
        int type = reader.getType();
        switch (type) {
            case PEER_JOIN:
                System.out.println(reader.readUTF() + " has connected to the chat. Say hello!");
                break;
            case CHAT_MESSAGE:
                System.out.println(reader.readUTF() + " says: " + reader.readUTF());
                break;
            case PEER_DISCONNECT:
                System.out.println(reader.readUTF() + " has left the chat.");
                break;
            case PRIVATE_CHAT_MESSAGE:
                System.out.println(reader.readUTF() + " whispers to you: " + reader.readUTF());
                break;
            case PRINT:
                System.out.println(reader.readUTF());
                break;
        }
    }

    private void readWelcomeMesssage() {
        File f = new File("localdata" + File.separator + "welcome.txt");
        ByteArray ba = ByteArray.readFromFileAndRewind(f);
        System.out.println();
        System.out.println(ba.readUTFBytes(ba.getBytesAvailable()));
        System.out.println();
    }
}
