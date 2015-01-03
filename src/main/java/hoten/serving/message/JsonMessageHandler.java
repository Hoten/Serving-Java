package hoten.serving.message;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hoten.serving.SocketHandler;
import java.io.UnsupportedEncodingException;

public abstract class JsonMessageHandler<S extends SocketHandler> extends MessageHandler<S, JsonObject> {

    @Override
    public JsonObject interpret(byte[] bytes) throws UnsupportedEncodingException {
        String json = new String(bytes, "UTF-8");
        Gson gson = new Gson();
        return gson.fromJson(json, JsonElement.class).getAsJsonObject();
    }
}
