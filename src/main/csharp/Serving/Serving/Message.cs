using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Text;

namespace Serving
{
    class Compressor
    {
        public byte[] Compress(byte[] data)
        {
            using (var outStream = new MemoryStream())
            {
                using (var tinyStream = new GZipStream(outStream, CompressionMode.Compress))
                using (var mStream = new MemoryStream(data))
                    mStream.CopyTo(tinyStream);
                return outStream.ToArray();
            }
        }
    }

    class Decompressor
    {
        public byte[] Uncompress(byte[] data)
        {
            using (var inStream = new MemoryStream(data))
            using (var bigStream = new GZipStream(inStream, CompressionMode.Decompress))
            using (var bigStreamOut = new MemoryStream())
            {
                bigStream.CopyTo(bigStreamOut);
                return bigStreamOut.ToArray();
            }
        }
    }

    public class Message
    {
        public static Message OutboundMessage(Protocol protocol, byte[] data)
        {
            return new Message(protocol, protocol.Compress ? new Compressor().Compress(data) : data);
        }

        public static Message InboundMessage(Protocol protocol, byte[] data)
        {
            return new Message(protocol, protocol.Compress ? new Decompressor().Uncompress(data) : data);
        }

        public Protocol Protocol { get; private set; }
        public byte[] Data { get; private set; }

        public Message(Protocol protocol, byte[] data)
        {
            Protocol = protocol;
            Data = data;
        }

        public Object Interpret()
        {
            switch (Protocol.Method)
            {
                case DataMethod.JSON:
                    return InterpretAsJson();
                case DataMethod.BINARY:
                    return InterpretAsBinary();
            }
            return null;
        }

        private Dictionary<String, String> InterpretAsJson()
        {
            var json = Encoding.UTF8.GetString(Data);
            return JsonConvert.DeserializeObject<Dictionary<String, String>>(json);
        }

        private BinaryReader InterpretAsBinary()
        {
            var stream = new MemoryStream(Data);
            return new BigEndianBinaryReader(stream, Encoding.UTF8);
        }
    }
}
