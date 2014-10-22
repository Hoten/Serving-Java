package hoten.serving;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hoten.serving.Protocols.Protocol;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;

public final class Message {

    public static Message OutboundMessage(Protocol protocol, byte[] data) {
        Message message = new Message(protocol, protocol.compress ? new Compressor().compress(data) : data);
        return message;
    }

    public static Message InboundMessage(Protocol protocol, byte[] data) {
        Message message = new Message(protocol, protocol.compress ? new Decompressor().uncompress(data) : data);
        return message;
    }

    public final Protocol protocol;
    public final byte[] data;

    private Message(Protocol protocol, byte[] data) {
        this.protocol = protocol;
        this.data = data;
    }

    public Object interpret() throws UnsupportedEncodingException {
        switch (protocol.method) {
            case JSON:
                return interpretAsJson();
            case BINARY:
                return interpretAsBinary();
        }
        return data;
    }

    JsonObject interpretAsJson() throws UnsupportedEncodingException {
        String json = new String(data, "UTF-8");
        Gson gson = new Gson();
        return gson.fromJson(json, JsonElement.class).getAsJsonObject();
    }

    DataInputStream interpretAsBinary() {
        return new DataInputStream(new ByteArrayInputStream(data));
    }
}