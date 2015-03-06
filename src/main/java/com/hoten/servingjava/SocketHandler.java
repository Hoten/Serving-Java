package com.hoten.servingjava;

import com.hoten.servingjava.message.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface SocketHandler {

    void start(Runnable onConnectionSettled, SocketHandler topLevelSocketHandler) throws IOException, InstantiationException, IllegalAccessException;

    void send(Message message) throws IOException;

    void close();

    DataOutputStream getOutputStream();

    DataInputStream getInputStream();
}
