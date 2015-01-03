package client.protocols;

import client.ConnectionToChatServerHandler;
import com.google.gson.JsonObject;
import hoten.serving.message.JsonMessageHandler;

public class PeerJoin extends JsonMessageHandler<ConnectionToChatServerHandler> {

    @Override
    protected void handle(ConnectionToChatServerHandler connection, JsonObject data) {
        String username = data.get("username").getAsString();
        connection.getChat().announceNewUser(username);
    }
}
