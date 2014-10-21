package client;

import chat.ClientToServerProtocols;
import chat.ServerToClientProtocols;
import hoten.serving.ConnectionToServerHandler;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class ConnectionToChatServerHandler extends ConnectionToServerHandler {

    private final Chat _chat;
    private final ServerToClientProtocols serverProtocols = new ServerToClientProtocols();
    public final ClientToServerProtocols clientProtocols = new ClientToServerProtocols();

    public ConnectionToChatServerHandler(Chat chat, Socket socket) throws IOException {
        super(socket, new ServerToClientProtocols());
        _chat = chat;
    }

    @Override
    protected void onConnectionSettled() {
        _chat.startChat(this);
    }

    @Override
    protected void handleData(int type, Map data) throws IOException {
        //TODO very hard to read.
        if (type == serverProtocols.peerJoin.type) {
            _chat.announceNewUser((String) data.get("username"));
        } else if (type == serverProtocols.chatMessage.type) {
            _chat.global((String) data.get("from"), (String) data.get("msg"));
        } else if (type == serverProtocols.peerDisconnect.type) {
            _chat.announceDisconnect((String) data.get("username"));
        } else if (type == serverProtocols.privateMessage.type) {
            _chat.whisper((String) data.get("from"), (String) data.get("msg"));
        } else if (type == serverProtocols.print.type) {
            _chat.announce((String) data.get("msg"));
        }
    }

    @Override
    protected void handleData(int type, DataInputStream data) {
    }
}
