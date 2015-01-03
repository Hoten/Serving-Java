package client.protocols;

import client.ConnectionToChatServerHandler;
import com.google.gson.JsonObject;
import hoten.serving.message.JsonMessageHandler;

public class Print extends JsonMessageHandler<ConnectionToChatServerHandler> {

    @Override
    protected void handle(ConnectionToChatServerHandler connection, JsonObject data) {
        String msg = data.get("msg").getAsString();
        connection.getChat().announce(msg);
    }
}
