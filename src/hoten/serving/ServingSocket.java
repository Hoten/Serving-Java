package hoten.serving;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServingSocket<T extends SocketHandler> {

    private boolean _open;
    final private ScheduledExecutorService _heartbeatScheduler = Executors.newScheduledThreadPool(1);
    final private ServerSocket _socket;
    final private File _clientDataFolder;
    final private String _localDataFolderName;
    final private byte[] _clientDataHashes;
    final protected List<T> _clients = new CopyOnWriteArrayList();

    public ServingSocket(int port, int heartbeatDelay, File clientDataFolder, String localDataFolderName) throws IOException {
        _clientDataFolder = clientDataFolder;
        _localDataFolderName = localDataFolderName;
        _clientDataHashes = hashFiles();
        _socket = new ServerSocket(port);
        startHeartbeat(heartbeatDelay);
    }
    
    public ServingSocket(int port, int heartbeatDelay) throws IOException {
        this(port, heartbeatDelay, null, null);
    }

    protected abstract T makeNewConnection(Socket newConnection) throws IOException;

    public void startServer() {
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.execute(() -> {
            while (true) {
                try {
                    T newClient = makeNewConnection(_socket.accept());
                    newClient._out.writeUTF(_localDataFolderName);
                    sendFileHashes(newClient._out);
                    sendRequestedFiles(newClient._in, newClient._out);
                    newClient._onConnectionSettled.run();
                    exec.execute(newClient::processDataUntilClosed);
                    _clients.add(newClient);
                } catch (IOException ex) {
                    Logger.getLogger(ServingSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        _open = true;
    }

    public void sendToAll(ByteArray msg) {
        _clients.stream().forEach((c) -> {
            c.send(msg);
        });
    }
    
    public void sendToSome(ByteArray msg, Predicate<T> filter) {
        _clients.stream().filter(filter).forEach((c) -> {
            c.send(msg);
        });
    }

    public void sendToAllBut(ByteArray msg, SocketHandler client) {
        _clients.stream().filter((c) -> (c != client)).forEach((c) -> {
            c.send(msg);
        });
    }

    public void removeClient(T client) {
        _clients.remove(client);
    }

    public void close() {
        _clients.stream().forEach((c) -> {
            c.close();
        });
        _open = false;
    }

    private void sendFileHashes(DataOutputStream out) throws IOException {
        if (_clientDataHashes != null) {
            out.write(_clientDataHashes);
        } else {
            out.writeInt(0);
        }
    }

    private void sendRequestedFiles(DataInputStream in, DataOutputStream out) throws IOException {
        int numFilesToUpdate = in.readInt();
        out.writeInt(numFilesToUpdate);
        for (int i = 0; i < numFilesToUpdate; i++) {
            String fname = in.readUTF();
            byte[] fileBytes = ByteArray.readFromFileAsRawArray(new File(_clientDataFolder, fname));
            out.writeUTF(fname);
            out.writeInt(fileBytes.length);
            out.write(fileBytes);
        }
        in.read();//stall until client is done
    }

    private byte[] hashFiles() {
        if (!_clientDataFolder.exists()) {
            _clientDataFolder.mkdirs();
        }

        //build the hashes
        ByteArray hashes = new ByteArray();
        hashes.writeInt(0);//placeholder
        Stack<File> a = new Stack();
        a.addAll(Arrays.asList(_clientDataFolder.listFiles()));
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
                while ((f = f.getParentFile()) != null && !f.equals(_clientDataFolder)) {
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
        return hashes.getBytes();
    }

    private void startHeartbeat(int heartbeatDelay) {
        final ByteArray msg = new ByteArray();
        msg.setType(0);
        final Runnable heartbeat = () -> {
            sendToAll(msg);
        };
        _heartbeatScheduler.scheduleAtFixedRate(heartbeat, heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS);
    }
}
