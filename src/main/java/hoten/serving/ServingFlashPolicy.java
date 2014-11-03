package hoten.serving;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FlashPolicySocket.java
 *
 * Adobe Flash requires a policy file sent over port 843 in order to accept a
 * socket connection client-side.
 *
 * TODO: Load external policy file
 *
 * @author Hoten
 */
public class ServingFlashPolicy {

    final private static int FLASH_PORT = 843;
    final private ExecutorService _executor = Executors.newSingleThreadExecutor();
    final private ServerSocket _servingSocket;
    final private String _policyFile;

    public ServingFlashPolicy(String policyFile) throws IOException {
        _servingSocket = new ServerSocket(FLASH_PORT);
        _policyFile = policyFile;
    }

    public ServingFlashPolicy(String domain, int port) throws IOException {
        this("<cross-domain-policy><allow-access-from domain=\"" + domain + "\" to-ports=\"" + port + "\"/></cross-domain-policy>\0");
    }

    //WARNING: following two constructors are very unrestrictive and are not recommeneded.
    public ServingFlashPolicy(int port) throws IOException {
        this("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + port + "\"/></cross-domain-policy>\0");
    }

    public ServingFlashPolicy() throws IOException {
        this("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"8200\"/></cross-domain-policy>\0");
    }

    public void close() throws IOException {
        _executor.shutdownNow();
        _servingSocket.close();
    }

    public void start() {
        _executor.submit(() -> {
            while (true) {
                try (Socket socket = _servingSocket.accept(); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.println(_policyFile);
                }
            }
        });
    }
}
