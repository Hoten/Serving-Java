package hoten.serving.filetransferring;

import com.google.gson.Gson;
import hoten.serving.ServingJava;
import hoten.serving.SocketHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public abstract class ServingFileTransferring<S extends SocketHandler> extends ServingJava<S> {

    final private File _clientDataFolder;
    final private String _localDataFolderName;
    final private String _jsonClientDataHashes;

    public ServingFileTransferring(int port, File clientDataFolder, String localDataFolderName) throws IOException {
        super(port);
        _clientDataFolder = clientDataFolder;
        _localDataFolderName = localDataFolderName;
        _jsonClientDataHashes = hashFiles();
    }

    @Override
    public void setupNewClient(S newClient) throws IOException {
        newClient.getOutputStream().writeUTF(_localDataFolderName);
        sendFileHashes(newClient.getOutputStream());
        sendRequestedFiles(newClient.getInputStream(), newClient.getOutputStream());
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
        in.read(); //stall until client is done
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
                Logger.getLogger(ServingJava.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return new Gson().toJson(hashes, Map.class);
    }
}
