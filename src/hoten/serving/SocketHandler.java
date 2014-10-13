package hoten.serving;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class SocketHandler {

    final static int MAX_MSG_LENGTH = (2 << 23) - 1;
    final static int MAX_TYPE = (2 << 8) - 1;
    final Socket _socket;
    final DataInputStream _in;
    final DataOutputStream _out;
    boolean isOpen;
    Runnable _onConnectionSettled;

    public SocketHandler(Socket socket) throws IOException {
        _socket = socket;
        _in = new DataInputStream(socket.getInputStream());
        _out = new DataOutputStream(socket.getOutputStream());
        isOpen = true;
    }

    public void onConnectionSettled(Runnable runnable) {
        _onConnectionSettled = runnable;
    }

    protected abstract void handleData(ByteArrayReader reader) throws IOException;

    final public boolean isOpen() {
        return isOpen;
    }
    
    public void send(ByteArrayWriter writer) {
        send(writer.getType(), writer.toByteArray());
    }

    public void send(int messageType, byte[] messageData) {
        if (!isOpen) {
            return;
        }
        int messageLength = messageData.length;
        if (messageLength > MAX_MSG_LENGTH) {
            System.out.println("Message is too big! " + messageLength);
            return;
        }
        if (messageType > MAX_TYPE) {
            System.out.println("Type is too high! " + messageType);
            return;
        }
        try {
            synchronized (_out) {
                _out.write(messageLength >> 16);
                _out.write(messageLength >> 8);
                _out.write(messageLength);
                _out.write(messageType);
                _out.write(messageData);
            }
        } catch (IOException ex) {
            close();
        }
    }

    public void close() {
        if (!isOpen) {
            return;
        }
        isOpen = false;
        try {
            _out.close();
            _in.close();
            _socket.close();
        } catch (IOException ex) {
            System.out.println("Error closing streams " + ex);
        }
    }

    private void handleData() {
        try {
            //read the buffer and message type
            int buffer = (_in.read() << 16) + (_in.read() << 8) + _in.read();
            int type = _in.read();

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
                int avail = _in.available();
                if (avail + bytesLoaded > buffer) {
                    avail = buffer - bytesLoaded;
                }

                //if eos, kill
                if (_in.read(bytes, bytesLoaded, avail) == -1) {
                    close();
                    return;
                }
                bytesLoaded += avail;
            } while (bytesLoaded < buffer);

            //if it is not a heartbeat message...
            if (type != 0) {
                //pass the data through a reader and call a function to deal with it
                ByteArrayReader reader = new ByteArrayReader(bytes);
                reader.setType(type);
                handleData(reader);
            }
        } catch (IOException ex) {
            close();
        }
    }

    protected void processDataUntilClosed() {
        while (isOpen) {
            handleData();
        }
    }
}
