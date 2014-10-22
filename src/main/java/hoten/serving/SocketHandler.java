package hoten.serving;

import com.google.gson.JsonObject;
import hoten.serving.Protocols.Protocol;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class SocketHandler {

    final Socket _socket;
    final DataInputStream _in;
    final DataOutputStream _out;
    final Protocols _protocols;
    final Protocols.BoundDest _boundFrom;
    final Protocols.BoundDest _boundTo;
    boolean isOpen;

    public SocketHandler(Socket socket, Protocols protocols, Protocols.BoundDest boundTo) throws IOException {
        _socket = socket;
        _protocols = protocols;
        _boundTo = boundTo;
        _boundFrom = boundTo == Protocols.BoundDest.CLIENT ? Protocols.BoundDest.SERVER : Protocols.BoundDest.CLIENT;
        _in = new DataInputStream(socket.getInputStream());
        _out = new DataOutputStream(socket.getOutputStream());
        isOpen = true;
    }

    protected abstract void onConnectionSettled();

    protected abstract void handleData(int type, JsonObject data) throws IOException;

    protected abstract void handleData(int type, DataInputStream data) throws IOException;

    public void send(Message message) {
        if (!isOpen) {
            return;
        }
        try {
            synchronized (_out) {
                _out.writeInt(message.data.length);
                _out.writeInt(message.protocol.type);
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
        byte[] bytes = new byte[buffer];
        _in.readFully(bytes);
        Message message = Message.InboundMessage(_protocols.get(_boundFrom, type), bytes);
        Object interpreted = message.interpret();
        if (interpreted instanceof JsonObject) {
            handleData(type, (JsonObject) interpreted);
        } else if (interpreted instanceof DataInputStream) {
            handleData(type, (DataInputStream) interpreted);
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

    protected Protocol outbound(Enum protocolEnum) {
        return _protocols.get(_boundTo, protocolEnum.ordinal());
    }

    final public boolean isOpen() {
        return isOpen;
    }
}
