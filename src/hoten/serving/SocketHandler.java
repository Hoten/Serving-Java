package hoten.serving;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SocketHandler {

    final static int MAX_MSG_LENGTH = (1 << 24) - 1;
    final static int MAX_TYPE = (1 << 9) - 1;
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

    public void send(ByteArrayWriter writer) {
        send(writer.getType(), writer.toByteArray());
    }

    public void send(int messageType, byte[] messageData) {
        if (!isOpen) {
            return;
        }
        int messageLength = messageData.length;
        if (messageLength > MAX_MSG_LENGTH) {
            Logger.getLogger(ServerConnectionHandler.class.getName()).log(Level.SEVERE, "Message is too big! {0}", messageLength);
            return;
        }
        if (messageType > MAX_TYPE) {
            Logger.getLogger(ServerConnectionHandler.class.getName()).log(Level.SEVERE, "Type is too high! {0}", messageType);
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
            int buffer = (_in.read() << 16) + (_in.read() << 8) + _in.read();
            int type = _in.read();
            byte[] bytes = new byte[buffer];
            ByteArrayReader reader = new ByteArrayReader(bytes);
            reader.setType(type);
            handleData(reader);
        } catch (IOException ex) {
            close();
        }
    }

    protected void processDataUntilClosed() {
        while (isOpen) {
            handleData();
        }
    }

    final public boolean isOpen() {
        return isOpen;
    }
}
