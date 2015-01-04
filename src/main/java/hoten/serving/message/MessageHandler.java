package hoten.serving.message;

import hoten.serving.SocketHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.reflections.Reflections;

// :( extends SocketHandler???
public abstract class MessageHandler<S extends SocketHandler, T> {

    private final static Map<String, Class> _handlersBySimpleName = new HashMap<>();

    public static Class get(String simpleName) {
        return _handlersBySimpleName.get(simpleName);
    }

    // :(
    public static void loadMessageHandlers(List<String> packages) {
        Reflections reflections = new Reflections();
        reflections.getSubTypesOf(MessageHandler.class).stream()
                .filter(klass -> packages.stream().anyMatch(pkg -> klass.getName().startsWith(pkg + ".")))
                .forEach(klass -> {
                    _handlersBySimpleName.put(klass.getSimpleName(), klass);
                });
    }

    protected abstract T interpret(byte[] bytes) throws UnsupportedEncodingException;

    protected abstract void handle(S connection, T data) throws IOException;

    public void handle(S connection, byte[] bytes) throws IOException, UnsupportedEncodingException {
        handle(connection, interpret(bytes));
    }
}
