using Newtonsoft.Json.Linq;
using Serving;
using System;

namespace ChatClient.Protocol
{
    class PeerDisconnect : JsonMessageHandler<ConnectionToChatServerHandler>
    {
        protected override void Handle(ConnectionToChatServerHandler connection, JObject data)
        {
            var username = data["username"].Value<String>();
            connection.Chat.AnnounceDisconnect(username);
        }
    }
}
