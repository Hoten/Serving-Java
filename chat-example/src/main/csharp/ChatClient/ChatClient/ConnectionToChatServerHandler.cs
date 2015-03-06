using Serving;
using Serving.FileTransferring;
using System;

namespace ChatClient
{
    class ConnectionToChatServerHandler : SocketHandler
    {
        public Chat Chat { get; private set; }

        private FileTransferringSocketReciever _socketHandler;

        public ConnectionToChatServerHandler(String host, int port, Chat chat)
        {
            _socketHandler = new FileTransferringSocketReciever(new SocketHandlerImpl(host, port));
            Chat = chat;
        }

        public void SendUsername(String username)
        {
            var message = new JsonMessageBuilder()
                    .Type("SetUsername")
                    .Set("username", username)
                    .Build();
            Send(message);
        }

        public void Quit()
        {
            var message = new JsonMessageBuilder()
                    .Type("LogOff")
                    .Build();
            Send(message);
        }

        public void SendMessage(String msg)
        {
            var message = new JsonMessageBuilder()
                    .Type("ChatMessage")
                    /*.Compressed(true)*/ // :(
                    .Set("msg", msg)
                    .Build();
            Send(message);
        }

        public void SendWhisper(String to, String msg)
        {
            var message = new JsonMessageBuilder()
                    .Type("Whisper")
                    .Set("to", to)
                    .Set("msg", msg)
                    .Build();
            Send(message);
        }

        public void Start(Action onConnectionSettled, SocketHandler topLevelSocketHandler)
        {
            _socketHandler.Start(onConnectionSettled, topLevelSocketHandler);
        }
        
        public void Send(Message message)
        {
            _socketHandler.Send(message);
        }

        public void Close()
        {
            _socketHandler.Close();
        }

        public JavaBinaryReader GetInputStream()
        {
            return _socketHandler.GetInputStream();
        }

        public JavaBinaryWriter GetOutputStream()
        {
            return _socketHandler.GetOutputStream();
        }

        public String GetLocalDataFolder()
        {
            return _socketHandler.LocalDataFolder;
        }
    }
}
