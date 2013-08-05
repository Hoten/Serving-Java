package hoten.serving;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class FlashPolicySocket extends Thread {

    final private static int FLASH_PORT = 843;
    final private ServerSocket socket;
    final private String policyFile;
    private boolean open;

    public FlashPolicySocket(String policyFile) throws IOException {
        super("Flash Policy Socket");
        socket = new ServerSocket(FLASH_PORT);
        this.policyFile = policyFile;
    }

    public FlashPolicySocket(String domain, int port) throws IOException {
        this("<cross-domain-policy><allow-access-from domain=\"" + domain + "\" to-ports=\"" + port + "\"/></cross-domain-policy>\0");
    }
    
    //WARNING: following two constructors are very unrestrictive and are not recommeneded.

    public FlashPolicySocket(int port) throws IOException {
        this("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + port + "\"/></cross-domain-policy>\0");
    }

    public FlashPolicySocket() throws IOException {
        this("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"8200\"/></cross-domain-policy>\0");
    }

    public void close() {
        open = false;
    }

    @Override
    public void run() {
        open = true;
        while (open) {
            try {
                //wait for a new connection...
                Socket s = socket.accept();
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                        s.getInputStream()));

                //send policy file to new client
                out.println(policyFile);

                //close stream and socket
                out.close();
                in.close();
                s.close();
            } catch (IOException ex) {
                Logger.getLogger(FlashPolicySocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
