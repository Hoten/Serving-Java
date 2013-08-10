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
    private boolean isOpen;

    public SocketHandler(Socket socket, DATA_SIZE IN_MSG_SIZE, DATA_SIZE IN_MSG_TYPE, DATA_SIZE OUT_MSG_SIZE, DATA_SIZE OUT_MSG_TYPE) throws IOException {
        this.socket = socket;
        this.IN_MSG_SIZE = IN_MSG_SIZE;
        this.IN_MSG_TYPE = IN_MSG_TYPE;
        this.OUT_MSG_SIZE = OUT_MSG_SIZE;
        this.OUT_MSG_TYPE = OUT_MSG_TYPE;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        isOpen = true;
        startReadingThread();
    }

    public SocketHandler(Socket socket) throws IOException {
        this(socket, DATA_SIZE.INT, DATA_SIZE.BYTE, DATA_SIZE.INT, DATA_SIZE.BYTE);
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
            int buffer = 0, type = 0;

            //read the buffer and message type
            if (IN_MSG_SIZE == DATA_SIZE.INT) {
                buffer = in.readInt();
            } else if (IN_MSG_SIZE == DATA_SIZE.SHORT) {
                buffer = in.readShort();
            } else if (IN_MSG_SIZE == DATA_SIZE.BYTE) {
                buffer = in.readByte();
            }

            if (IN_MSG_TYPE == DATA_SIZE.INT) {
                type = in.readInt();
            } else if (IN_MSG_TYPE == DATA_SIZE.SHORT) {
                type = in.readShort();
            } else if (IN_MSG_TYPE == DATA_SIZE.BYTE) {
                type = in.readByte();
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

            //if it is not a heartbeat message...
            if (type != 0) {
                //pass the data through a reader and call a function to deal with it
                ByteArray reader = new ByteArray(bytes);
                reader.setType(type);
                reader.rewind();
                handleData(reader);
            }

            //recursively call if there is another message to read
            if (in.available() > IN_MSG_SIZE.length + IN_MSG_TYPE.length) {
                handleData();
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
