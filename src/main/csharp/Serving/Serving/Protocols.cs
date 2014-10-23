using System.Collections.Generic;

namespace Serving
{
    public class Protocols
    {
        private List<Protocol> _serverBound = new List<Protocol>();
        private List<Protocol> _clientBound = new List<Protocol>();

        protected Protocol Add(BoundDest boundTo, int type, DataMethod method, bool compress)
        {
            Protocol p = new Protocol(boundTo, type, method, compress);
            Get(boundTo).Add(p);
            return p;
        }

        public Protocol Get(BoundDest boundTo, int type)
        {
            return Get(boundTo)[type];
        }

        private List<Protocol> Get(BoundDest boundTo)
        {
            return boundTo == BoundDest.SERVER ? _serverBound : _clientBound;
        }
    }
}
