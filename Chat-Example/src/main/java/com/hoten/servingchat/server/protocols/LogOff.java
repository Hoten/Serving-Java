package com.hoten.servingchat.server.protocols;

import com.google.gson.JsonObject;
import com.hoten.servingjava.message.JsonMessageHandler;
import com.hoten.servingchat.server.ConnectionToChatClientHandler;

// :( Dataless message handler?
public class LogOff extends JsonMessageHandler<ConnectionToChatClientHandler> {

    @Override
    protected void handle(ConnectionToChatClientHandler connection, JsonObject data) {
        connection.close();
    }
}
