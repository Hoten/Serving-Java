package hoten.serving;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ServingSocket.java
 *
 * Extend this class to act as a server. See the chat example.
 *
 * @author Hoten
 */
public abstract class ServingSocket extends Thread {

    private ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
    final private ServerSocket socket;
    final protected CopyOnWriteArrayList<ClientConnectionHandler> clients = new CopyOnWriteArrayList();
    private File clientDataFolder;
    private byte[] clientDataHashes;
    private boolean open;

    public ServingSocket(int port, int heatbeatDelay) throws IOException {
        super("Serving Socket " + port);
        socket = new ServerSocket(port);
        clientDataHashes = null;

        //start heartbeat
        final ByteArray msg = new ByteArray();
        msg.setType(0);
        final Runnable heartbeat = new Runnable() {
            @Override
            public void run() {
                sendToAll(msg);
            }
        };
        heartbeatScheduler.scheduleAtFixedRate(heartbeat, heatbeatDelay, heatbeatDelay, TimeUnit.MILLISECONDS);
    }

    //use this constructor if you want to transfer data files to clients
    public ServingSocket(int port, int heatbeatDelay, File clientDataFolder) throws IOException {
        this(port, heatbeatDelay);
        this.clientDataFolder = clientDataFolder;

        if (!clientDataFolder.exists()) {
            clientDataFolder.mkdirs();
        }

        //build the hashes
        ByteArray hashes = new ByteArray();
        hashes.writeInt(0);//placeholder
        Stack<File> a = new Stack();
        a.addAll(Arrays.asList(clientDataFolder.listFiles()));
        int numFiles = 0;
        while (!a.isEmpty()) {
            File cur = a.pop();
            if (cur.isDirectory()) {
                a.addAll(Arrays.asList(cur.listFiles()));
            } else {
                String path = cur.getName();
                File f = cur;
                ByteArray ba = ByteArray.readFromFile(f);

                //build the relative path to the clientDataFolder
                //TODO use stringbuilder. find a way to do it when adding to begining
                while ((f = f.getParentFile()) != null && !f.equals(clientDataFolder)) {
                    path = f.getName() + File.separator + path;
                }

                hashes.writeUTF(path);
                hashes.writeUTF(ba.getMD5Hash());
                numFiles++;
            }
        }
        int eof = hashes.getPos();
        hashes.setPos(0);
        hashes.writeInt(numFiles);
        hashes.setPos(eof);
        clientDataHashes = hashes.getBytes();
    }

    public void sendToAll(ByteArray msg) {
        for (SocketHandler c : clients) {
            c.send(msg);
        }
    }

    public void sendToAllBut(ByteArray msg, ClientConnectionHandler client) {
        for (SocketHandler c : clients) {
            if (c != client) {
                c.send(msg);
            }
        }
    }

    @Override
    public void run() {
        open = true;
        while (open) {
            try {
                ClientConnectionHandler newClient = makeNewConnection(socket.accept());

                DataInputStream in = newClient.in;
                DataOutputStream out = newClient.out;

                //send hash info
                if (clientDataHashes != null) {
                    out.write(clientDataHashes);
                }else{
                    out.writeInt(0);
                }

                //handle file requests
                int numFilesToUpdate = in.readInt();
                for (int i = 0; i < numFilesToUpdate; i++) {
                    String fname = in.readUTF();
                    byte[] fileBytes = ByteArray.readFromFileAsRawArray(new File(clientDataFolder, fname));
                    out.writeUTF(fname);
                    out.writeInt(fileBytes.length);
                    out.write(fileBytes);
                }
                in.read();//stall until client is done

                newClient.startReadingThread();
                clients.add(newClient);
            } catch (IOException ex) {
                System.out.println("error making new connection: " + ex);
            }
        }
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("error closing server: " + ex);
        }
    }

    public void close() {
        for (SocketHandler c : clients) {
            c.close();
        }
        open = false;
    }

    public void removeClient(ClientConnectionHandler client) {
        clients.remove(client);
    }

    protected abstract ClientConnectionHandler makeNewConnection(Socket newConnection) throws IOException;
}
