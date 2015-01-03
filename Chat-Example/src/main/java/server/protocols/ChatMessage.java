package server.protocols;

import com.google.gson.JsonObject;
import hoten.serving.message.JsonMessageHandler;
import hoten.serving.message.JsonMessageBuilder;
import hoten.serving.message.Message;
import server.ConnectionToChatClientHandler;

public class ChatMessage extends JsonMessageHandler<ConnectionToChatClientHandler> {

    @Override
    protected void handle(ConnectionToChatClientHandler connection, JsonObject data) {
        Message message = new JsonMessageBuilder()
                .type("ChatMessage")
                .set("from", connection.getUsername())
                .set("msg", data.get("msg").getAsString())
                .build();
        connection.server.sendToAllBut(message, connection);
    }
}
