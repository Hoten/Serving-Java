using Serving;
using System;
using System.Collections.Generic;
using System.IO;

namespace ChatClient
{
    class ConnectionToChatServerHandler : ConnectionToServerHandler
    {
        private Chat _chat;

        public ConnectionToChatServerHandler(Chat chat, String host, int port)
            : base(host, port, new ChatProtocols(), BoundDest.SERVER)
        {
            _chat = chat;
        }

        protected override void OnConnectionSettled()
        {
            _chat.StartChat(this);
        }

        protected override void HandleData(int type, Dictionary<String, String> data)
        {
            switch ((ChatProtocols.Clientbound)type)
            {
                case ChatProtocols.Clientbound.PeerJoin:
                    _chat.AnnounceNewUser(data["username"]);
                    break;
                case ChatProtocols.Clientbound.ChatMessage:
                    _chat.Global(data["from"], data["msg"]);
                    break;
                case ChatProtocols.Clientbound.PeerDisconnect:
                    _chat.AnnounceDisconnect(data["username"]);
                    break;
                case ChatProtocols.Clientbound.PrivateMessage:
                    _chat.Whisper(data["from"], data["msg"]);
                    break;
                case ChatProtocols.Clientbound.Print:
                    _chat.Announce(data["msg"]);
                    break;
            }
        }

        protected override void HandleData(int type, BinaryReader data) { }

        public void SendUsername(String username)
        {
            Message message = new JsonMessageBuilder()
                    .Protocol(Outbound(ChatProtocols.Serverbound.SetUsername))
                    .Set("username", username)
                    .Build();
            Send(message);
        }

        public void Quit()
        {
            Message message = new JsonMessageBuilder()
                    .Protocol(Outbound(ChatProtocols.Serverbound.LogOff))
                    .Build();
            Send(message);
        }

        public void SendMessage(String msg)
        {
            Message message = new JsonMessageBuilder()
                    .Protocol(Outbound(ChatProtocols.Serverbound.ChatMessage))
                    .Set("msg", msg)
                    .Build();
            Send(message);
        }

        public void SendWhisper(String to, String msg)
        {
            Message message = new JsonMessageBuilder()
                    .Protocol(Outbound(ChatProtocols.Serverbound.PrivateMessage))
                    .Set("to", to)
                    .Set("msg", msg)
                    .Build();
            Send(message);
        }
    }
}
