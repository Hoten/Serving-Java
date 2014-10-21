package hoten.serving;

import java.util.ArrayList;
import java.util.List;

public class Protocols {

    public enum DataMethod {

        JSON, BINARY
    }

    public class Protocol {

        public final int type;
        public final DataMethod method;
        public final boolean compress;

        Protocol(int type, DataMethod method, boolean compress) {
            this.type = type;
            this.method = method;
            this.compress = compress;
        }
    }

    private final List<Protocol> _protocols = new ArrayList();
    
    protected Protocol add(DataMethod method, boolean compress) {
        Protocol p = new Protocol(_protocols.size(), method, compress);
        _protocols.add(p);
        return p;
    }

    public Protocol get(int type) {
        return _protocols.get(type);
    }
}
