package com.hoten.servingchat.server.protocols;

import com.google.gson.JsonObject;
import com.hoten.servingjava.message.JsonMessageHandler;
import com.hoten.servingjava.message.JsonMessageBuilder;
import com.hoten.servingjava.message.Message;
import com.hoten.servingchat.server.ConnectionToChatClientHandler;

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
