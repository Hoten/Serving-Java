package hoten.serving;

import com.google.gson.Gson;
import hoten.serving.Protocols.Protocol;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonMessageBuilder {
    private final Map _map = new HashMap();
    private Protocol _protocol;
    
    public JsonMessageBuilder set(String key, Object value) {
        _map.put(key, value);
        return this;
    }
    
    public JsonMessageBuilder protocol(Protocol protocol) { 
        _protocol = protocol;
        return this;
    }
    
    public Message build() {
        byte[] data = null;
        try {
            data = new Gson().toJson(_map, Map.class).getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(JsonMessageBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Message.OutboundMessage(_protocol, data);
    }
}