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

    //DATA_SIZE allows for customization in the bitsize of message properties (read: buffer and type)
    public enum DATA_SIZE {

        BYTE(1), SHORT(2), INT(4);
        final int length;

        DATA_SIZE(int length) {
            this.length = length;
        }
    }
    final private Socket socket;
    final private DataInputStream in;
    final private DataOutputStream out;
    final private DATA_SIZE IN_MSG_SIZE;
    final private DATA_SIZE IN_MSG_TYPE;
    final private DATA_SIZE OUT_MSG_SIZE;
    final private DATA_SIZE OUT_MSG_TYPE;

    public SocketHandler(Socket socket, DATA_SIZE IN_MSG_SIZE, DATA_SIZE IN_MSG_TYPE, DATA_SIZE OUT_MSG_SIZE, DATA_SIZE OUT_MSG_TYPE) throws IOException {
        this.socket = socket;
        this.IN_MSG_SIZE = IN_MSG_SIZE;
        this.IN_MSG_TYPE = IN_MSG_TYPE;
        this.OUT_MSG_SIZE = OUT_MSG_SIZE;
        this.OUT_MSG_TYPE = OUT_MSG_TYPE;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    final public boolean send(int type, ByteArray message) throws IOException {
        byte[] b = message.getBytes();
        int messageLength = b.length;
        try {
            synchronized (out) {
                if (OUT_MSG_SIZE == DATA_SIZE.INT) {
                    out.writeInt(messageLength);
                } else if (OUT_MSG_SIZE == DATA_SIZE.SHORT) {
                    out.writeShort(messageLength);
                } else if (OUT_MSG_SIZE == DATA_SIZE.BYTE) {
                    out.writeByte(messageLength);
                }
                if (OUT_MSG_TYPE == DATA_SIZE.INT) {
                    out.writeInt(type);
                } else if (OUT_MSG_TYPE == DATA_SIZE.SHORT) {
                    out.writeShort(type);
                } else if (OUT_MSG_TYPE == DATA_SIZE.BYTE) {
                    out.writeByte(type);
                }
                out.write(b);
            }
            return true;
        } catch (IOException ex) {
            close();
        }
        return false;
    }

    final public void handleData() throws IOException {
        if (in.available() >= IN_MSG_SIZE.length + IN_MSG_TYPE.length) {
            int buffer = 0, t = 0;

            //read the buffer and message type

            if (IN_MSG_SIZE == DATA_SIZE.INT) {
                buffer = in.readInt();
            } else if (IN_MSG_SIZE == DATA_SIZE.SHORT) {
                buffer = in.readShort();
            } else if (IN_MSG_SIZE == DATA_SIZE.BYTE) {
                buffer = in.readByte();
            }

            if (IN_MSG_TYPE == DATA_SIZE.INT) {
                t = in.readInt();
            } else if (IN_MSG_TYPE == DATA_SIZE.SHORT) {
                t = in.readShort();
            } else if (IN_MSG_TYPE == DATA_SIZE.BYTE) {
                t = in.readByte();
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
                in.read(bytes, bytesLoaded, avail);
                bytesLoaded += avail;
            } while (bytesLoaded < buffer);

            //pass the data through a reader and call a function to deal with it
            ByteArray reader = new ByteArray(bytes);
            reader.rewind();
            handleData(t, reader);

            //recursively call if there is another message to read
            if (in.available() > IN_MSG_SIZE.length + IN_MSG_TYPE.length) {
                handleData();
            }
        }
    }

    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }

    protected abstract void handleData(int type, ByteArray reader) throws IOException;
}
