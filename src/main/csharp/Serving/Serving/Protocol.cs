using System;

namespace Serving
{
    public enum BoundDest { SERVER, CLIENT }
    public enum DataMethod { JSON, BINARY }

    public class Protocol
    {
        public BoundDest BoundTo { get; private set; }
        public int Type { get; private set; }
        public DataMethod Method { get; private set; }
        public bool Compress { get; private set; }

        public Protocol(BoundDest boundTo, int type, DataMethod method, Boolean compress)
        {
            BoundTo = boundTo;
            Type = type;
            Method = method;
            Compress = compress;
        }
    }
}
