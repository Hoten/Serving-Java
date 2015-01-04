package hoten.serving.filetransferring;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hoten.serving.SocketHandler;
import hoten.serving.message.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public final class FileTransferringSocketReciever implements SocketHandler {

    private final SocketHandler _socketHandler;
    private final DataOutputStream _out;
    private final DataInputStream _in;
    private File _localDataFolder;

    public FileTransferringSocketReciever(SocketHandler socketHandler) {
        _socketHandler = socketHandler;
        _out = socketHandler.getOutputStream();
        _in = socketHandler.getInputStream();
    }

    @Override
    public void start(Runnable onConnectionSettled, SocketHandler topLevelSocketHandler) throws IOException, InstantiationException, IllegalAccessException {
        _localDataFolder = new File(_in.readUTF());
        _localDataFolder.mkdirs();
        respondToHashes();
        readNewFilesFromServer();
        _socketHandler.start(onConnectionSettled, topLevelSocketHandler);
    }

    @Override
    public void send(Message message) throws IOException {
        _socketHandler.send(message);
    }

    @Override
    public void close() {
        _socketHandler.close();
    }

    private void respondToHashes() throws IOException {
        Map<String, byte[]> hashes = readFileHashesFromServer();
        Collection<File> localFiles = FileUtils.listFiles(_localDataFolder, null, true);
        Collection<String> filesToRequest = compareFileHashes(localFiles, hashes);
        _socketHandler.getOutputStream().writeUTF(new Gson().toJson(filesToRequest, List.class));
        localFiles.stream().forEach((f) -> {
            f.delete();
        });
    }

    private Map<String, byte[]> readFileHashesFromServer() throws IOException {
        String jsonHashes = _socketHandler.getInputStream().readUTF();
        Type type = new TypeToken<Map<String, byte[]>>() {
        }.getType();
        return new Gson().fromJson(jsonHashes, type);
    }

    private Collection<String> compareFileHashes(Collection<File> files, Map<String, byte[]> hashes) {
        List<String> filesToRequest = new ArrayList();
        hashes.forEach((fileName, fileHash) -> {
            try {
                File f = new File(_localDataFolder, fileName);

                for (File cf : files) {
                    if (cf.equals(f)) {
                        //remove this file as a candidate for pruning
                        files.remove(cf);
                        break;
                    }
                }

                if (!f.exists() || !Arrays.equals(DigestUtils.md5(FileUtils.readFileToString(f)), fileHash)) {
                    filesToRequest.add(fileName);
                }
            } catch (IOException ex) {
                Logger.getLogger(FileTransferringSocketReciever.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return filesToRequest;
    }

    private void readNewFilesFromServer() throws IOException {
        int numFiles = _in.readInt();
        for (int i = 0; i < numFiles; i++) {
            String fileName = _in.readUTF();
            int length = _in.readInt();
            Logger.getLogger(FileTransferringSocketReciever.class.getName()).log(Level.INFO, "Updating {0}, size = {1}", new Object[]{fileName, length});
            byte[] data = new byte[length];
            _in.readFully(data);
            FileUtils.writeByteArrayToFile(new File(_localDataFolder, fileName), data);
        }
        _out.write(0); //done updating files
    }

    @Override
    public DataOutputStream getOutputStream() {
        return _socketHandler.getOutputStream();
    }

    @Override
    public DataInputStream getInputStream() {
        return _socketHandler.getInputStream();
    }

    public File getLocalDataFolder() {
        return _localDataFolder;
    }
}
