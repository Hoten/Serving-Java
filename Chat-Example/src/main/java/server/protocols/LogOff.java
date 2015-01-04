package server.protocols;

import com.google.gson.JsonObject;
import hoten.serving.message.JsonMessageHandler;
import server.ConnectionToChatClientHandler;

// :( Dataless message handler?
public class LogOff extends JsonMessageHandler<ConnectionToChatClientHandler> {

    @Override
    protected void handle(ConnectionToChatClientHandler connection, JsonObject data) {
        connection.close();
    }
}
