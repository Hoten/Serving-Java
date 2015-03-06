package com.hoten.servingchat.server.protocols;

import com.google.gson.JsonObject;
import com.hoten.servingjava.message.JsonMessageHandler;
import com.hoten.servingjava.message.JsonMessageBuilder;
import com.hoten.servingjava.message.Message;
import com.hoten.servingchat.server.ConnectionToChatClientHandler;

public class SetUsername extends JsonMessageHandler<ConnectionToChatClientHandler> {

    @Override
    protected void handle(ConnectionToChatClientHandler connection, JsonObject data) {
        String username = data.get("username").getAsString();
        connection.setUsername(username);
        Message message = new JsonMessageBuilder()
                .type("PeerJoin")
                .set("username", username)
                .build();
        connection.getServingChat().sendToAllBut(message, connection);
        System.out.println(username + " has joined!");
    }
}
