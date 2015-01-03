package client.protocols;

import client.ConnectionToChatServerHandler;
import com.google.gson.JsonObject;
import hoten.serving.message.JsonMessageHandler;

public class ChatMessage extends JsonMessageHandler<ConnectionToChatServerHandler> {

    @Override
    protected void handle(ConnectionToChatServerHandler connection, JsonObject data) {
        String from = data.get("from").getAsString();
        String msg = data.get("msg").getAsString();
        connection.getChat().global(from, msg);
    }
}
