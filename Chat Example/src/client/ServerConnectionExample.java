package client;

import hoten.serving.ByteArray;
import hoten.serving.ServingSocket;
import hoten.serving.SocketHandler;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import server.ClientConnectionExample;

/**
 * Client.java
 *
 * Extends SocketHandler, and provides the logic for dealing with data from the
 * server.
 *
 * @author Hoten
 */
public class ServerConnectionExample extends SocketHandler {

    final public static int PEER_JOIN = 1;
    final public static int CHAT_MESSAGE = 2;
    final public static int PEER_DISCONNECT = 3;
    final public static int PRIVATE_CHAT_MESSAGE = 4;
    final public static int PRINT = 5;
    final public static int HASHES = 6;
    final public static int NEW_FILE = 7;
    final public static int FINISHED_UPDATING = 8;

    public ServerConnectionExample(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    protected void handleData(ByteArray reader) throws IOException {
        int type = reader.getType();
        ByteArray msg;
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
            case HASHES:
                msg = ServingSocket.clientRespondToHashes(reader);
                msg.setType(ClientConnectionExample.REQUEST_FILE_UPDATES);
                //if files are already up to date...
                if (msg.getSize() == 0) {
                    System.out.println("All files were up to date!");
                    readWelcomeMesssage();
                } else {
                    msg.send();
                }
                break;
            case NEW_FILE:
                System.out.println("Downloading new file...");
                String fileName = reader.readUTF();
                int len = reader.readInt();
                byte[] b = reader.readBytes(len);
                System.out.println("Downloading " + fileName + ", size = " + len);
                ByteArray.saveAs(new File("localdata" + File.separator + fileName), b);
                break;
            case FINISHED_UPDATING:
                readWelcomeMesssage();
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
