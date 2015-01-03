using Newtonsoft.Json.Linq;
using Serving;
using System;

namespace ChatClient.Protocol
{
    class Print : JsonMessageHandler<ConnectionToChatServerHandler>
    {
        protected override void Handle(ConnectionToChatServerHandler connection, JObject data)
        {
            var msg = data["msg"].Value<String>();
            connection.Chat.Announce(msg);
        }
    }
}
