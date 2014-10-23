package hoten.serving;

import java.util.ArrayList;
import java.util.List;

public class Protocols {

    public static enum BoundDest {

        SERVER, CLIENT
    }

    public static enum DataMethod {

        JSON, BINARY
    }

    public class Protocol {

        public final BoundDest boundTo;
        public final int type;
        public final DataMethod method;
        public final boolean compress;

        Protocol(BoundDest boundTo, int type, DataMethod method, boolean compress) {
            this.boundTo = boundTo;
            this.type = type;
            this.method = method;
            this.compress = compress;
        }
    }

    private final List<Protocol> _serverBound = new ArrayList();
    private final List<Protocol> _clientBound = new ArrayList();

    protected Protocol add(BoundDest boundTo, int type, DataMethod method, boolean compress) {
        Protocol p = new Protocol(boundTo, type, method, compress);
        get(boundTo).add(p);
        return p;
    }

    public Protocol get(BoundDest boundTo, int type) {
        return get(boundTo).get(type);
    }

    private List<Protocol> get(BoundDest boundTo) {
        return boundTo == BoundDest.SERVER ? _serverBound : _clientBound;
    }
}
