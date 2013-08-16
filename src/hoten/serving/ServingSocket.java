package hoten.serving;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    final protected CopyOnWriteArrayList<SocketHandler> clients = new CopyOnWriteArrayList();
    private File clientDataFolder;
    private ByteArray clientDataHashes;
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
        clientDataHashes = new ByteArray();
        Stack<File> a = new Stack();
        a.addAll(Arrays.asList(clientDataFolder.listFiles()));
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

                clientDataHashes.writeUTF(path);
                clientDataHashes.writeUTF(ba.getMD5Hash());
            }
        }
        clientDataHashes.trim();
    }

    //send each file in it's own ByteArray message
    public int getFilesForClient(ByteArray requests, SocketHandler client, int newFileType, int finishedType) {
        int numSent = 0;
        while (requests.getBytesAvailable() > 0) {
            String fileName = requests.readUTF();
            ByteArray ba = new ByteArray();
            ba.setType(newFileType);
            ByteArray fileBytes = ByteArray.readFromFile(new File(clientDataFolder, fileName));
            ba.writeUTF(fileName);
            ba.writeInt(fileBytes.getSize());
            ba.writeBytes(fileBytes);
            ba.trim();
            //ba.compress();
            client.send(ba);
            numSent++;
        }
        ByteArray ba = new ByteArray();
        ba.setType(finishedType);
        client.send(ba);
        return numSent;
    }

    public ByteArray getClientDataHashes() {
        return clientDataHashes;
    }

    public void sendToAll(ByteArray msg) {
        for (SocketHandler c : clients) {
            c.send(msg);
        }
    }

    public void sendToAllBut(ByteArray msg, SocketHandler client) {
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
                SocketHandler newClient = makeNewConnection(socket.accept());
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

    public void removeClient(SocketHandler client) {
        clients.remove(client);
    }

    protected abstract SocketHandler makeNewConnection(Socket newConnection) throws IOException;

    /**
     *
     * CLIENT SIDE - for use on java clients
     *
     * reader = message from server contains MD5 hashes of all the necessary
     * client data files...music, graphics, etc. see how clientDataHashes is
     * built for reference
     *
     * if the server hash != the client's local hash, the file does not exist,
     * or if there exists files/folders in the localdata folder that the server
     * does not explicitly list, then delete the excess files/folders
     *
     * returns a byte array of file names that the client needs to request
     *
     * also prunes the client's localdata of files that are not in the server's
     * clientdata folder
     *
     */
    public static ByteArray clientRespondToHashes(ByteArray reader) {
        ByteArray msg = new ByteArray();
        File local = new File("localdata");

        //ensure it exists
        if (!local.exists()) {
            local.mkdirs();
        }

        //create an arraylist of all the files in the localdata folder
        ArrayList<File> currentf = new ArrayList();
        Stack<File> all = new Stack();
        all.addAll(Arrays.asList(local.listFiles()));
        while (!all.isEmpty()) {
            File cur = all.pop();
            if (cur.isDirectory()) {
                all.addAll(Arrays.asList(cur.listFiles()));
                currentf.add(cur);
            } else {
                currentf.add(cur);
            }
        }

        //now lets go through the hashes and compare with the local files
        while (reader.getBytesAvailable() > 0) {
            String fileName = reader.readUTF();
            String fileHash = reader.readUTF();
            File f = new File("localdata" + File.separator + fileName);

            //remove this file as a candidate for pruning
            for (File cf : currentf) {
                if (cf.equals(f)) {
                    currentf.remove(cf);
                    break;
                }
            }

            //if locally the file doesn't exist or the hash is wrong, request update
            if (!f.exists() || !ByteArray.readFromFile(f).getMD5Hash().equals(fileHash)) {
                msg.writeUTF(fileName);
            }
        }

        /**
         * currentf should now only contains files that were not listed by the
         * server,but are still in the localdata folder. we do not want to keep
         * these files,so let's delete them. also should have all the
         * directories, but that is okay because they can't be deleted if they
         * contain files.
         */
        for (File f : currentf) {
            f.delete();
        }

        msg.trim();
        return msg;
    }
}
