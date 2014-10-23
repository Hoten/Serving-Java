using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Text;

namespace Serving
{
    public class JsonMessageBuilder
    {
        private Dictionary<String, Object> _map = new Dictionary<String, Object>();
        private Protocol _protocol;

        public JsonMessageBuilder Set(String key, Object value)
        {
            _map[key] = value;
            return this;
        }

        public JsonMessageBuilder Protocol(Protocol protocol)
        {
            _protocol = protocol;
            return this;
        }

        public Message Build()
        {
            var json = JsonConvert.SerializeObject(_map);
            var data = Encoding.UTF8.GetBytes(json);
            return Message.OutboundMessage(_protocol, data);
        }
    }
}
