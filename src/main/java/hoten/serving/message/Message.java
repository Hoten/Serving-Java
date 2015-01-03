package hoten.serving.message;

import hoten.serving.fileutils.Compressor;

public final class Message {

    public final byte[] data;
    public final String type;
    public final boolean compressed;

    public Message(byte[] data, String type, boolean compressed) {
        this.type = type;
        this.compressed = compressed;
        this.data = compressed ? new Compressor().compress(data) : data;
    }
}
