package chat;

import hoten.serving.Protocols;

public class ClientToServerProtocols extends Protocols {
    public Protocol setUsername = add(DataMethod.JSON, false);
    public Protocol privateMessage = add(DataMethod.JSON, false);
    public Protocol logOff = add(DataMethod.JSON, false);
    public Protocol chatMessage = add(DataMethod.JSON, false);
}
