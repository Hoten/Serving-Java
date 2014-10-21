package hoten.serving;

import com.google.gson.Gson;
import hoten.serving.Protocols.Protocol;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public abstract class SocketHandler {

    final Socket _socket;
    final DataInputStream _in;
    final DataOutputStream _out;
    final Protocols _protocols;
    boolean isOpen;

    public SocketHandler(Socket socket, Protocols protocols) throws IOException {
        _socket = socket;
        _protocols = protocols;
        _in = new DataInputStream(socket.getInputStream());
        _out = new DataOutputStream(socket.getOutputStream());
        isOpen = true;
    }

    protected abstract void onConnectionSettled();

    protected abstract void handleData(int type, Map data) throws IOException;

    protected abstract void handleData(int type, DataInputStream data) throws IOException;

    public void send(Message message) {
        if (!isOpen) {
            return;
        }
        try {
            synchronized (_out) {
                _out.writeInt(message.data.length);
                _out.writeInt(message.type);
                _out.write(message.data);
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

    private void handleData() throws IOException {
        int buffer = _in.readInt();
        int type = _in.readInt();
        Protocol protocol = _protocols.get(type);
        byte[] bytes = new byte[buffer];
        _in.readFully(bytes);
        if (protocol.compress) {
            bytes = new Decompressor().uncompress(bytes);
        }
        if (protocol.method == Protocols.DataMethod.JSON) {
            String json = new String(bytes, "UTF-8");
            Gson gson = new Gson();
            Map data = gson.fromJson(json, Map.class);
            handleData(type, data);
        } else if (protocol.method == Protocols.DataMethod.BINARY) {
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
            handleData(type, data);
        }
    }

    protected void processDataUntilClosed() {
        try {
            while (isOpen) {
                handleData();
            }
        } catch (IOException ex) {
            close();
        }
    }

    final public boolean isOpen() {
        return isOpen;
    }
}
