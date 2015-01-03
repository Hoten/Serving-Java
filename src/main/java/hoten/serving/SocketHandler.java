package hoten.serving;

import hoten.serving.message.MessageHandler;
import hoten.serving.fileutils.Decompressor;
import hoten.serving.message.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SocketHandler {

    final Socket _socket;
    final DataInputStream _in;
    final DataOutputStream _out;

    public SocketHandler(Socket socket) throws IOException {
        _socket = socket;
        _in = new DataInputStream(socket.getInputStream());
        _out = new DataOutputStream(socket.getOutputStream());
    }

    protected abstract void onConnectionSettled() throws IOException;

    public void send(Message message) throws IOException {
        synchronized (_out) {
            _out.writeInt(message.data.length);
            _out.writeUTF(message.type);
            _out.writeBoolean(message.compressed);
            _out.write(message.data);
        }
    }

    public void closeIfOpen() {
        if (!_socket.isClosed()) {
            close();
        }
    }

    protected void close() {
        try {
            _out.close();
            _in.close();
            _socket.close();
        } catch (IOException ex) {
            System.err.println("Error closing streams " + ex);
        }
    }

    private void handleData() throws IOException, InstantiationException, IllegalAccessException {
        int dataSize = _in.readInt();
        String type = _in.readUTF();
        boolean compressed = _in.readBoolean();
        byte[] bytes = new byte[dataSize];
        _in.readFully(bytes);
        // :(
        if (compressed) {
            bytes = new Decompressor().uncompress(bytes);
        }

        Class handlerKlass = MessageHandler.get(type);
        MessageHandler ins = (MessageHandler) handlerKlass.newInstance();
        ins.handle(this, bytes);
    }

    protected void processDataUntilClosed() {
        try {
            while (true) {
                handleData();
            }
        } catch (IOException ex) {
            closeIfOpen();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
            closeIfOpen();
        }
    }
}
