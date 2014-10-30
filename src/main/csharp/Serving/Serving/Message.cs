using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Text;

namespace Serving
{
    static class StreamUtils {
        public static void CopyTo(this Stream input, Stream output)
        {
            byte[] buffer = new byte[16 * 1024]; // Fairly arbitrary size
            int bytesRead;

            while ((bytesRead = input.Read(buffer, 0, buffer.Length)) > 0)
            {
                output.Write(buffer, 0, bytesRead);
            }
        }
    }

    class Compressor
    {
        public byte[] Compress(byte[] data)
        {
            using (var outStream = new MemoryStream())
            {
                using (var tinyStream = new GZipStream(outStream, CompressionMode.Compress))
                using (var mStream = new MemoryStream(data))
                    StreamUtils.CopyTo(mStream, outStream);
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
                StreamUtils.CopyTo(bigStream, bigStreamOut);
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

        private JObject InterpretAsJson()
        {
            var json = Encoding.UTF8.GetString(Data);
            return JObject.Parse(json);
        }

        private JavaBinaryReader InterpretAsBinary()
        {
            var stream = new MemoryStream(Data);
            return new JavaBinaryReader(stream);
        }
    }
}
