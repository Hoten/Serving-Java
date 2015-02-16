package hoten.serving;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServingPolicyFile {

    public static void main(String[] args) throws IOException {
        new ServingPolicyFile(1044).start();
    }

    final private static int FLASH_PORT = 843;
    final private static String POLICY_FILE_FORMAT = "<?xml version='1.0'?>\n<cross-domain-policy>\n\t<allow-access-from domain=\"%s\" to-ports=\"%s\" />\n</cross-domain-policy>";
    final private ExecutorService _executor = Executors.newSingleThreadExecutor();
    final private ServerSocket _servingSocket;
    final private String _policyFile;
    final private String _requestString = "<policy-file-request/>\0";

    public ServingPolicyFile(String policyFile) throws IOException {
        _servingSocket = new ServerSocket(FLASH_PORT);
        _policyFile = policyFile;
    }

    public ServingPolicyFile(String domain, String ports) throws IOException {
        this(String.format(POLICY_FILE_FORMAT, domain, ports));
    }

    public ServingPolicyFile(int port) throws IOException {
        this(String.format(POLICY_FILE_FORMAT, "*", port));
    }

    public void close() throws IOException {
        _executor.shutdownNow();
        _servingSocket.close();
    }

    public void start() {
        _executor.submit(() -> {
            while (true) {
                try (Socket socket = _servingSocket.accept(); BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    for (int i = 0; i < _requestString.length(); i++) {
                        if (_requestString.charAt(i) != in.read()) {
                            socket.close();
                        }
                    }

                    if (!socket.isClosed()) {
                        out.write(_policyFile.getBytes());
                        out.flush();
                    }
                }
            }
        });
    }
}
