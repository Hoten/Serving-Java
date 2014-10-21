package chat;

import hoten.serving.Protocols;

public class ServerToClientProtocols extends Protocols{
    public Protocol peerJoin = add(DataMethod.JSON, false);
    public Protocol chatMessage = add(DataMethod.JSON, false);
    public Protocol peerDisconnect = add(DataMethod.JSON, false);
    public Protocol privateMessage = add(DataMethod.JSON, false);
    public Protocol print = add(DataMethod.JSON, false);
}
