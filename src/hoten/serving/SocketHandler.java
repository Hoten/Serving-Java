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
abstract class SocketHandler {

    final static int MAX_MSG_LENGTH = (2 << 23) - 1;
    final static int MAX_TYPE = (2 << 8) - 1;
    final Socket socket;
    final DataInputStream in;
    final DataOutputStream out;
    boolean isOpen;

    public SocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        isOpen = true;
    }

    //this function is called after all files have been updated
    protected abstract void onConnectionSettled() throws IOException;

    protected abstract void handleData(ByteArray reader) throws IOException;

    abstract void startReadingThread();

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
                System.out.println("Type is too high! " + type);
                return;
            }
            try {
                synchronized (out) {
                    out.write(messageLength >> 16);
                    out.write(messageLength >> 8);
                    out.write(messageLength);
                    out.write(type);
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
            int buffer = (in.read() << 16) + (in.read() << 8) + in.read();
            int type = in.read();

            //if eos, kill
            if (type == -1) {
                close();
                return;
            }

            //load the data into a byte array
            //DataInputStream must read in chunks until the entire message is read
            byte[] bytes = new byte[buffer];
            int bytesLoaded = 0;
            do {
                int avail = in.available();
                if (avail + bytesLoaded > buffer) {
                    avail = buffer - bytesLoaded;
                }

                //if eos, kill
                if (in.read(bytes, bytesLoaded, avail) == -1) {
                    close();
                    return;
                }
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

    void processDataUntilClosed() {
        while (isOpen) {
            handleData();
        }
    }
}
