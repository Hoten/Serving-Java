package chat;

import hoten.serving.Protocols;

public class ChatProtocols extends Protocols {

    public enum Clientbound {

        PeerJoin, ChatMessage, PeerDisconnect, PrivateMessage, Print;

    }

    public enum Serverbound {

        SetUsername, PrivateMessage, LogOff, ChatMessage
    }

    public ChatProtocols() {
        add(BoundDest.CLIENT, 0, DataMethod.JSON, false);
        add(BoundDest.CLIENT, 1, DataMethod.JSON, false);
        add(BoundDest.CLIENT, 2, DataMethod.JSON, false);
        add(BoundDest.CLIENT, 3, DataMethod.JSON, false);
        add(BoundDest.CLIENT, 4, DataMethod.JSON, false);
        
        add(BoundDest.SERVER, 0, DataMethod.JSON, false);
        add(BoundDest.SERVER, 1, DataMethod.JSON, false);
        add(BoundDest.SERVER, 2, DataMethod.JSON, false);
        add(BoundDest.SERVER, 3, DataMethod.JSON, false);
    }
}
