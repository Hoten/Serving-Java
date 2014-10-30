using Serving;

namespace ChatClient
{
    class ChatProtocols : Protocols
    {
        public enum Clientbound { PeerJoin, ChatMessage, PeerDisconnect, PrivateMessage, Print }
        public enum Serverbound { SetUsername, PrivateMessage, LogOff, ChatMessage }

        public ChatProtocols()
        {
            Add(BoundDest.CLIENT, 0, DataMethod.JSON, false);
            Add(BoundDest.CLIENT, 1, DataMethod.JSON, false);
            Add(BoundDest.CLIENT, 2, DataMethod.JSON, false);
            Add(BoundDest.CLIENT, 3, DataMethod.JSON, false);
            Add(BoundDest.CLIENT, 4, DataMethod.JSON, false);

            Add(BoundDest.SERVER, 0, DataMethod.JSON, false);
            Add(BoundDest.SERVER, 1, DataMethod.JSON, false);
            Add(BoundDest.SERVER, 2, DataMethod.JSON, false);
            Add(BoundDest.SERVER, 3, DataMethod.JSON, false);
        }
    }
}
