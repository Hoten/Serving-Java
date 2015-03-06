package com.hoten.servingchat.client.protocols;

import com.hoten.servingchat.client.ConnectionToChatServerHandler;
import com.google.gson.JsonObject;
import com.hoten.servingjava.message.JsonMessageHandler;

public class Print extends JsonMessageHandler<ConnectionToChatServerHandler> {

    @Override
    protected void handle(ConnectionToChatServerHandler connection, JsonObject data) {
        String msg = data.get("msg").getAsString();
        connection.getChat().announce(msg);
    }
}
