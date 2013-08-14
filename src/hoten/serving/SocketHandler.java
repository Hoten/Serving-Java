package hoten.serving;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * SocketHandler.java
 *
 * Wrapper for a Socket. Allows for easy reading, writing, and processing of
 * data over a network.
 *
 * @author Hoten
 */
public abstract class SocketHandler {

    final private static int MAX_MSG_LENGTH = (2 << 23) - 1;
    final private static int MAX_TYPE = (2 << 8) - 1;
    final private Socket socket;
    final private DataInputStream in;
    final private DataOutputStream out;
    private boolean isOpen;

    public SocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        isOpen = true;
        startReadingThread();
    }

    protected abstract void handleData(ByteArray reader) throws IOException;

    final public boolean isOpen() {
        return isOpen;
    }

    public void send(ByteArray message) {
        if (isOpen) {
            byte[] b = message.getBytes();
            int messageLength = b.length;
            int type = message.getType();
            if (messageLength > MAX_MSG_LENGTH) {
                System.out.println("Message is too big! " + messageLength);
                return;
            }
            if (type > MAX_TYPE) {
                System.out.println("Type it too high! " + type);
                return;
            }
            try {
                synchronized (out) {
                    out.write(type);
                    out.write(messageLength >> 16);
                    out.write(messageLength >> 8);
                    out.write(messageLength);
                    out.write(b);
                }
            } catch (IOException ex) {
                close();
            }
        }
    }

    public void close() {
        if (isOpen) {
            isOpen = false;
            try {
                out.close();
                in.close();
                socket.close();
            } catch (IOException ex) {
                System.out.println("Error closing streams " + ex);
            }
        }
    }

    private void handleData() {
        try {
            //read the buffer and message type
            int type = in.read();
            int buffer = (in.read() << 16) + (in.read() << 8) + in.read();

            //load the data into a byte array
            //DataInputStream must read in chunks until the entire message is read
            byte[] bytes = new byte[buffer];
            int bytesLoaded = 0;
            do {
                int avail = in.available();
                if (avail + bytesLoaded > buffer) {
                    avail = buffer - bytesLoaded;
                }
                in.read(bytes, bytesLoaded, avail);
                bytesLoaded += avail;
            } while (bytesLoaded < buffer);

            //if it is not a heartbeat message...
            if (type != 0) {
                //pass the data through a reader and call a function to deal with it
                ByteArray reader = new ByteArray(bytes);
                reader.setType(type);
                reader.rewind();
                handleData(reader);
            }
        } catch (IOException ex) {
            close();
        }
    }

    private void startReadingThread() {
        new Thread("handle data for: " + socket.getInetAddress()) {
            @Override
            public void run() {
                while (isOpen) {
                    handleData();
                }
            }
        }.start();
    }
}
