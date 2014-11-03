package hoten.serving;

import hoten.serving.Protocols.Protocol;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class BinaryMessageBuilder {

    private final ByteArrayOutputStream _out;
    private Protocol _protocol;

    public BinaryMessageBuilder() {
        _out = new ByteArrayOutputStream();
    }

    public BinaryMessageBuilder protocol(Protocol protocol) {
        _protocol = protocol;
        return this;
    }

    public BinaryMessageBuilder writeByte(int v) {
        _out.write(v);
        return this;
    }

    public BinaryMessageBuilder writeShort(int v) {
        _out.write((v >>> 8) & 0xFF);
        _out.write((v >>> 0) & 0xFF);
        return this;
    }

    public BinaryMessageBuilder writeInt(int v) {
        _out.write((v >>> 24) & 0xFF);
        _out.write((v >>> 16) & 0xFF);
        _out.write((v >>> 8) & 0xFF);
        _out.write((v >>> 0) & 0xFF);
        return this;
    }

    public BinaryMessageBuilder writeLong(long v) {
        _out.write((byte) ((v >>> 56) & 0xFF));
        _out.write((byte) ((v >>> 48) & 0xFF));
        _out.write((byte) ((v >>> 40) & 0xFF));
        _out.write((byte) ((v >>> 32) & 0xFF));
        _out.write((byte) ((v >>> 24) & 0xFF));
        _out.write((byte) ((v >>> 16) & 0xFF));
        _out.write((byte) ((v >>> 8) & 0xFF));
        _out.write((byte) ((v >>> 0) & 0xFF));
        return this;
    }

    public BinaryMessageBuilder writeFloat(float v) {
        return writeInt(Float.floatToIntBits(v));
    }

    public BinaryMessageBuilder writeDouble(double v) {
        return writeLong(Double.doubleToLongBits(v));
    }

    public BinaryMessageBuilder writeBoolean(boolean v) {
        _out.write(v ? 1 : 0);
        return this;
    }

    public BinaryMessageBuilder writeBytes(byte[] bytes) {
        _out.write(bytes, 0, bytes.length);
        return this;
    }

    public BinaryMessageBuilder writeUTF(String v) {
        byte[] bytes = v.getBytes();
        writeShort(bytes.length);
        return writeBytes(bytes);
    }

    public BinaryMessageBuilder writeFile(File file) throws IOException {
        return writeBytes(Files.readAllBytes(file.toPath()));
    }

    public Message build() {
        return Message.outboundMessage(_protocol, _out.toByteArray());
    }
}
