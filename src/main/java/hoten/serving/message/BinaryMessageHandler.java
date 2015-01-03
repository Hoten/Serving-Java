package hoten.serving.message;

import hoten.serving.SocketHandler;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public abstract class BinaryMessageHandler<S extends SocketHandler> extends MessageHandler<S, DataInputStream> {

    @Override
    public DataInputStream interpret(byte[] bytes) {
        return new DataInputStream(new ByteArrayInputStream(bytes));
    }
}
