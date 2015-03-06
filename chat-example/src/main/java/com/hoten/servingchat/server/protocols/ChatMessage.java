package com.hoten.servingchat.server.protocols;

import com.google.gson.JsonObject;
import com.hoten.servingjava.message.JsonMessageHandler;
import com.hoten.servingjava.message.JsonMessageBuilder;
import com.hoten.servingjava.message.Message;
import com.hoten.servingchat.server.ConnectionToChatClientHandler;

public class ChatMessage extends JsonMessageHandler<ConnectionToChatClientHandler> {

    @Override
    protected void handle(ConnectionToChatClientHandler connection, JsonObject data) {
        Message message = new JsonMessageBuilder()
                .type("ChatMessage")
                .set("from", connection.getUsername())
                .set("msg", data.get("msg").getAsString())
                .build();
        connection.getServingChat().sendToAllBut(message, connection);
    }
}
