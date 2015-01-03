package hoten.serving.message;

public final class Message {

    public final byte[] data;
    public final String type;
    public final boolean compressed;

    public Message(byte[] data, String type, boolean compressed) {
        this.data = data;
        this.type = type;
        this.compressed = compressed;
    }
}
