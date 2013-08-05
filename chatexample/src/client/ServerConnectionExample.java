package client;

import hoten.serving.ByteArray;
import hoten.serving.SocketHandler;
import java.io.IOException;
import java.net.Socket;

/**
 * Client.java Function Date Aug 5, 2013
 *
 * @author Hoten
 */
public class ServerConnectionExample extends SocketHandler {

    final public static int PEER_JOIN = 1;
    final public static int CHAT_MESSAGE = 2;
    final public static int PEER_DISCONNECT = 3;
    final public static int PRIVATE_CHAT_MESSAGE = 4;

    public ServerConnectionExample(Socket socket) throws IOException {
        super(socket, SocketHandler.DATA_SIZE.SHORT, SocketHandler.DATA_SIZE.BYTE, SocketHandler.DATA_SIZE.SHORT, SocketHandler.DATA_SIZE.BYTE);
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
        }
    }
}
