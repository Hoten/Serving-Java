using Serving;
using System;

namespace ChatClient
{
    class ConnectionToChatServerHandler : ConnectionToServerHandler
    {
        public Chat Chat { get; private set; }

        public ConnectionToChatServerHandler(Chat chat, String host, int port)
            : base(host, port)
        {
            Chat = chat;
        }

        protected override void OnConnectionSettled()
        {
            Chat.StartChat(this);
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
    }
}
