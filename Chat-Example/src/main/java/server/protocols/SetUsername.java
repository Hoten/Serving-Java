package server.protocols;

import com.google.gson.JsonObject;
import hoten.serving.message.JsonMessageHandler;
import hoten.serving.message.JsonMessageBuilder;
import hoten.serving.message.Message;
import server.ConnectionToChatClientHandler;

public class SetUsername extends JsonMessageHandler<ConnectionToChatClientHandler> {

    @Override
    protected void handle(ConnectionToChatClientHandler connection, JsonObject data) {
        String username = data.get("username").getAsString();

        connection.setUsername(username);

        Message message = new JsonMessageBuilder()
                .type("PeerJoin")
                .set("username", username)
                .build();

        connection.server.sendToAllBut(message, connection);
    }
}
