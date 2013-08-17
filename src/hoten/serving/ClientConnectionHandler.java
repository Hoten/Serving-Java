package hoten.serving;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientConnectionHandler.java
 *
 * Extend this class to handle data from clients server-side. See chat example
 * for more details.
 *
 * @author Hoten
 */
public abstract class ClientConnectionHandler extends SocketHandler {

    public ClientConnectionHandler(Socket s) throws IOException {
        super(s);
    }

    @Override
    void startReadingThread() {
        final ClientConnectionHandler THIS = this;
        new Thread("handle data for: " + socket.getInetAddress()) {
            @Override
            public void run() {
                try {
                    onConnectionSettled();
                    processDataUntilClosed();
                } catch (IOException ex) {
                    Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
                    close();
                }
            }
        }.start();
    }
}
