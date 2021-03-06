package com.hoten.servingjava;

import com.hoten.servingjava.message.MessageHandler;
import com.hoten.servingjava.fileutils.Decompressor;
import com.hoten.servingjava.message.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public final class SocketHandlerImpl implements SocketHandler {

    private final Socket _socket;
    private final DataInputStream _in;
    private final DataOutputStream _out;

    public SocketHandlerImpl(Socket socket) throws IOException {
        _socket = socket;
        _in = new DataInputStream(socket.getInputStream());
        _out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void start(Runnable onConnectionSettled, SocketHandler topLevelSocketHandler) throws IOException, InstantiationException, IllegalAccessException {
        onConnectionSettled.run();
        while (true) {
            handleData(topLevelSocketHandler);
        }
    }

    @Override
    public synchronized void send(Message message) throws IOException {
        _out.writeInt(message.data.length);
        _out.writeUTF(message.type);
        _out.writeBoolean(message.compressed);
        _out.write(message.data);
    }

    @Override
    public synchronized void close() {
        if (_socket.isClosed()) {
            return;
        }
        try {
            _out.close();
            _in.close();
            _socket.close();
        } catch (IOException ex) {
            System.err.println("Error closing streams " + ex);
        }
    }

    // because the decorator pattern is being used, we need a reference to the top
    // most SocketHandler in order to access application-level details found in that
    // class. TODO: Avoid this. :(
    private void handleData(SocketHandler topLevelSocketHandler) throws IOException, InstantiationException, IllegalAccessException {
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
        if (handlerKlass != null) {
            MessageHandler ins = (MessageHandler) handlerKlass.newInstance();
            ins.handle(topLevelSocketHandler, bytes);
        } else {
            throw new InstantiationException("No such protocol: " + type);
        }
    }

    @Override
    public DataOutputStream getOutputStream() {
        return _out;
    }

    @Override
    public DataInputStream getInputStream() {
        return _in;
    }
}
