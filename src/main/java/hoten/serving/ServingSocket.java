package hoten.serving;

import com.google.gson.Gson;
import hoten.serving.Protocols.Protocol;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO abstract out file stuff
public abstract class ServingSocket<T extends SocketHandler> {

    final private ServerSocket _socket;
    final private File _clientDataFolder;
    final private String _localDataFolderName;
    final private String _jsonClientDataHashes;
    final private Protocols _protocols;
    final protected List<T> _clients = new CopyOnWriteArrayList();

    public ServingSocket(int port, Protocols protocols, File clientDataFolder, String localDataFolderName) throws IOException {
        _protocols = protocols;
        _clientDataFolder = clientDataFolder;
        _localDataFolderName = localDataFolderName;
        _jsonClientDataHashes = hashFiles();
        _socket = new ServerSocket(port);
    }

    public ServingSocket(int port, Protocols protocols) throws IOException {
        this(port, protocols, null, null);
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
                    newClient.onConnectionSettled();
                    exec.execute(newClient::processDataUntilClosed);
                    _clients.add(newClient);
                } catch (IOException ex) {
                    Logger.getLogger(ServingSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void sendTo(Message message, T client) {
        try {
            client.send(message);
        } catch (IOException ex) {
            _clients.remove(client);
            client.closeIfOpen();
        }
    }

    public void sendTo(Message message, Predicate<T> selector) {
        _clients.stream().filter(selector).forEach((c) -> {
            sendTo(message, c);
        });
    }

    public void sendToFirst(Message message, Predicate<T> selector) {
        _clients.stream().filter(selector).findFirst().ifPresent((c) -> {
            sendTo(message, c);
        });
    }

    public void sendToAll(Message message) {
        sendTo(message, (c) -> true);
    }

    public void sendToAllBut(Message message, SocketHandler client) {
        sendTo(message, (c) -> (c != client));
    }

    public void close() {
        _clients.stream().forEach((c) -> {
            c.closeIfOpen();
        });
    }

    protected Protocol outbound(Enum protocolEnum) {
        return _protocols.get(Protocols.BoundDest.CLIENT, protocolEnum.ordinal());
    }

    private void sendFileHashes(DataOutputStream out) throws IOException {
        if (_jsonClientDataHashes != null) {
            out.writeUTF(_jsonClientDataHashes);
        } else {
            out.writeUTF("[]");
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
        List<File> files = FileUtils.getAllFilesInDirectory(_clientDataFolder);
        files.stream().forEach((file) -> {
            String relativePath = _clientDataFolder.toPath().relativize(file.toPath()).toString();
            byte[] hash = FileUtils.md5HashFile(file);
            hashes.put(relativePath, hash);
        });
        return new Gson().toJson(hashes, Map.class);
    }
}
