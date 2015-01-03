package hoten.serving;

import hoten.serving.message.Message;
import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

//TODO abstract out file stuff
public abstract class ServingSocket<S extends SocketHandler> {

    final private ExecutorService exec = Executors.newCachedThreadPool();
    final private ServerSocket _socket;
    final private File _clientDataFolder;
    final private String _localDataFolderName;
    final private String _jsonClientDataHashes;
    final protected List<S> _clients = new CopyOnWriteArrayList();

    public ServingSocket(int port, File clientDataFolder, String localDataFolderName) throws IOException {
        _clientDataFolder = clientDataFolder;
        _localDataFolderName = localDataFolderName;
        _jsonClientDataHashes = hashFiles();
        _socket = new ServerSocket(port);
    }

    public ServingSocket(int port) throws IOException {
        this(port, null, null);
    }

    protected abstract S makeNewConnection(Socket newConnection) throws IOException;

    public void startServer() {
        exec.execute(() -> {
            while (true) {
                try {
                    final S newClient = makeNewConnection(_socket.accept());
                    exec.execute(() -> setupNewClient(newClient));
                } catch (IOException ex) {
                    Logger.getLogger(ServingSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void setupNewClient(S newClient) {
        try {
            newClient._out.writeUTF(_localDataFolderName);
            sendFileHashes(newClient._out);
            sendRequestedFiles(newClient._in, newClient._out);
            newClient.onConnectionSettled();
            exec.execute(newClient::processDataUntilClosed);
            _clients.add(newClient);
        } catch (IOException ex) {
            Logger.getLogger(ServingSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendTo(Message message, S client) {
        try {
            client.send(message);
        } catch (IOException ex) {
            _clients.remove(client);
            client.closeIfOpen();
        }
    }

    public void sendTo(Message message, Predicate<S> selector) {
        _clients.stream().filter(selector).forEach((c) -> {
            sendTo(message, c);
        });
    }

    public void sendToFirst(Message message, Predicate<S> selector) {
        _clients.stream().filter(selector).findFirst().ifPresent((c) -> {
            sendTo(message, c);
        });
    }

    public void sendToAll(Message message) {
        sendTo(message, c -> true);
    }

    public void sendToAllBut(Message message, SocketHandler client) {
        sendTo(message, c -> c != client);
    }

    public void close() {
        exec.shutdown();
        _clients.stream().forEach(c -> {
            c.closeIfOpen();
        });
    }

    private void sendFileHashes(DataOutputStream out) throws IOException {
        if (_jsonClientDataHashes != null) {
            out.writeUTF(_jsonClientDataHashes);
        } else {
            out.writeUTF("[]"); // :(
        }
    }

    private void sendRequestedFiles(DataInputStream in, DataOutputStream out) throws IOException {
        String jsonFileNames = in.readUTF();
        List<String> fileNames = new Gson().fromJson(jsonFileNames, List.class);
        out.writeInt(fileNames.size());
        for (String fname : fileNames) {
            byte[] fileBytes = Files.readAllBytes(new File(_clientDataFolder, fname).toPath());
            out.writeUTF(fname);
            out.writeInt(fileBytes.length);
            out.write(fileBytes);
        }
        in.read();//stall until client is done
    }

    private String hashFiles() {
        Map<String, byte[]> hashes = new HashMap();
        Collection<File> files = FileUtils.listFiles(_clientDataFolder, null, true);
        files.stream().forEach(file -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                String relativePath = _clientDataFolder.toPath().relativize(file.toPath()).toString();
                byte[] hash = DigestUtils.md5(fis);
                hashes.put(relativePath, hash);
            } catch (IOException ex) {
                Logger.getLogger(ServingSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return new Gson().toJson(hashes, Map.class);
    }
}
