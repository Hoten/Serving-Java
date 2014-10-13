package hoten.serving;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServingSocket<T extends SocketHandler> {

    final private ScheduledExecutorService _heartbeatScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture _hearbeatFuture;
    final private ServerSocket _socket;
    final private File _clientDataFolder;
    final private String _localDataFolderName;
    final private byte[] _clientDataHashes;
    final protected List<T> _clients = new CopyOnWriteArrayList();

    public ServingSocket(int port, File clientDataFolder, String localDataFolderName) throws IOException {
        _clientDataFolder = clientDataFolder;
        _localDataFolderName = localDataFolderName;
        _clientDataHashes = hashFiles();
        _socket = new ServerSocket(port);
    }

    public ServingSocket(int port) throws IOException {
        this(port, null, null);
    }

    public void setHeartbeat(int heartbeatDelay) {
        if (_hearbeatFuture != null) {
            _hearbeatFuture.cancel(false);
        }
        final ByteArrayWriter msg = new ByteArrayWriter();
        msg.setType(0);
        final Runnable heartbeat = () -> {
            sendToAll(msg);
        };
        _hearbeatFuture = _heartbeatScheduler.scheduleAtFixedRate(heartbeat, heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS);
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
    }

    public void sendTo(ByteArrayWriter msg, Predicate<T> selector) {
        byte[] messageData = msg.toByteArray();
        int messageType = msg.getType();
        _clients.stream().filter(selector).forEach((c) -> {
            c.send(messageType, messageData);
        });
    }

    public void sendToFirst(ByteArrayWriter msg, Predicate<T> selector) {
        _clients.stream().filter(selector).findFirst().ifPresent((c) -> {
            c.send(msg.getType(), msg.toByteArray());
        });
    }

    public void sendToAll(ByteArrayWriter msg) {
        sendTo(msg, (c) -> true);
    }

    public void sendToAllBut(ByteArrayWriter msg, SocketHandler client) {
        sendTo(msg, (c) -> (c != client));
    }

    public void removeClient(T client) {
        _clients.remove(client);
    }

    public void close() {
        _heartbeatScheduler.shutdownNow();
        _clients.stream().forEach((c) -> {
            c.close();
        });
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
            ByteArrayReader bar = new ByteArrayReader(new File(_clientDataFolder, fname));
            byte[] fileBytes = bar.toByteArray();
            out.writeUTF(fname);
            out.writeInt(fileBytes.length);
            out.write(fileBytes);
        }
        in.read();//stall until client is done
    }

    private byte[] hashFiles() {
        ByteArrayWriter hashes = new ByteArrayWriter();
        List<File> files = FileUtils.getAllFilesInDirectory(_clientDataFolder);
        hashes.writeInt(files.size());
        files.stream().forEach((file) -> {
            String relativePath = _clientDataFolder.toPath().relativize(file.toPath()).toString();
            String hash = FileUtils.md5HashFile(file);
            hashes.writeUTF(relativePath);
            hashes.writeUTF(hash);
        });
        return hashes.toByteArray();
    }
}
