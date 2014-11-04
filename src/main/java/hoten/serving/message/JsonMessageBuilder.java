package hoten.serving.message;

import com.google.gson.Gson;
import hoten.serving.message.Protocols.Protocol;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonMessageBuilder {
    private final Map _map = new HashMap();
    private Protocol _protocol;
    private Gson _gson = new Gson();
    
    public JsonMessageBuilder set(String key, Object value) {
        _map.put(key, value);
        return this;
    }
    
    public JsonMessageBuilder protocol(Protocol protocol) { 
        _protocol = protocol;
        return this;
    }
    
    public JsonMessageBuilder gson(Gson gson) { 
        _gson = gson;
        return this;
    }
    
    public Message build() {
        byte[] data = null;
        try {
            data = _gson.toJson(_map, Map.class).getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(JsonMessageBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Message.outboundMessage(_protocol, data);
    }
}