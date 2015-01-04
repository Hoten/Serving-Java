package server.protocols;

import com.google.gson.JsonObject;
import hoten.serving.message.JsonMessageHandler;
import hoten.serving.message.JsonMessageBuilder;
import hoten.serving.message.Message;
import server.ConnectionToChatClientHandler;

public class Whisper extends JsonMessageHandler<ConnectionToChatClientHandler> {

    @Override
    protected void handle(ConnectionToChatClientHandler connection, JsonObject data) {
        String msg = data.get("msg").getAsString();
        String to = data.get("to").getAsString();
        Message message = new JsonMessageBuilder()
                .type("Whisper")
                .set("from", connection.getUsername())
                .set("msg", msg)
                .build();
        connection.getServingChat().sendToClientWithUsername(message, to);
    }
}
