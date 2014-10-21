package hoten.serving;

import hoten.serving.Protocols.Protocol;

public final class Message {

    public final int type;
    public final byte[] data;

    Message(Protocol protocol, byte[] data) {
        this.type = protocol.type;
        this.data = protocol.compress ? new Compressor().compress(data) : data;
    }
}
