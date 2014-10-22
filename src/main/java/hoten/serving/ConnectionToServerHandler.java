package hoten.serving;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ConnectionToServerHandler extends SocketHandler {

    public final File localDataFolder;

    public ConnectionToServerHandler(Socket socket, Protocols protocols) throws IOException {
        super(socket, protocols, Protocols.BoundDest.SERVER);
        localDataFolder = new File(_in.readUTF());
    }

    public void start() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                respondToHashes();
                readNewFilesFromServer();
                onConnectionSettled();
                processDataUntilClosed();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionToServerHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void readNewFilesFromServer() throws IOException {
        int numFiles = _in.readInt();
        for (int i = 0; i < numFiles; i++) {
            String fileName = _in.readUTF();
            int len = _in.readInt();
            Logger.getLogger(ConnectionToServerHandler.class.getName()).log(Level.INFO, "Updating {0}, size = {1}", new Object[]{fileName, len});
            byte[] b = new byte[len];
            _in.readFully(b);
            FileUtils.saveAs(new File(localDataFolder, fileName), b);
        }
        _out.write(0);//done updating files
    }

    private void respondToHashes() throws IOException {
        if (!localDataFolder.exists()) {
            localDataFolder.mkdirs();
        }
        List<File> localFiles = FileUtils.getAllFilesInDirectory(localDataFolder);
        Map<String, String> hashes = readFileHashesFromServer();
        List<String> filesToRequest = compareFileHashes(localFiles, hashes);
        _out.writeUTF(new Gson().toJson(filesToRequest, List.class));
        localFiles.stream().forEach((f) -> {
            f.delete();
        });
    }

    private Map<String, String> readFileHashesFromServer() throws IOException {
        String jsonHashes = _in.readUTF();
        return new Gson().fromJson(jsonHashes, Map.class);
    }

    private List<String> compareFileHashes(List<File> files, Map<String, String> hashes) {
        List<String> filesToRequest = new ArrayList();
        hashes.forEach((fileName, fileHash) -> {
            File f = new File(localDataFolder, fileName);

            for (File cf : files) {
                if (cf.equals(f)) {
                    //remove this file as a candidate for pruning
                    files.remove(cf);
                    break;
                }
            }

            if (!f.exists() || !FileUtils.md5HashFile(f).equals(fileHash)) {
                filesToRequest.add(fileName);
            }
        });
        return filesToRequest;
    }
}
