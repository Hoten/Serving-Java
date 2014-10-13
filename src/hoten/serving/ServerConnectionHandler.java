package hoten.serving;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ServerConnectionHandler extends SocketHandler {

    public final File localDataFolder;

    public ServerConnectionHandler(Socket socket) throws IOException {
        super(socket);
        localDataFolder = new File(_in.readUTF());
        ByteArrayWriter.serverConnection = this;
    }

    public void start() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                respondToHashes();
                readNewFilesFromServer();
                _onConnectionSettled.run();
                processDataUntilClosed();
            } catch (IOException ex) {
                Logger.getLogger(ServerConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void readNewFilesFromServer() throws IOException {
        int numFiles = _in.readInt();
        for (int i = 0; i < numFiles; i++) {
            String fileName = _in.readUTF();
            int len = _in.readInt();
            Logger.getLogger(ServerConnectionHandler.class.getName()).log(Level.INFO, "Updating {0}, size = {1}", new Object[]{fileName, len});
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
        List<String> fileNames = new ArrayList();
        List<String> fileHashes = new ArrayList();
        readFileHashesFromServer(fileNames, fileHashes);
        List<String> filesToRequest = compareFileHashes(localFiles, fileNames, fileHashes);

        _out.writeInt(filesToRequest.size());
        for (String fname : filesToRequest) {
            _out.writeUTF(fname);
        }

        localFiles.stream().forEach((f) -> {
            f.delete();
        });
    }

    private void readFileHashesFromServer(List<String> fileNames, List<String> fileHashes) throws IOException {
        int numFilesFromServer = _in.readInt();
        for (int i = 0; i < numFilesFromServer; i++) {
            fileNames.add(_in.readUTF());
            fileHashes.add(_in.readUTF());
        }
    }

    private List<String> compareFileHashes(List<File> files, List<String> fileNames, List<String> fileHashes) {
        List<String> filesToRequest = new ArrayList();
        int numFiles = fileNames.size();
        for (int i = 0; i < numFiles; i++) {
            String fileName = fileNames.get(i);
            String fileHash = fileHashes.get(i);
            File f = new File(localDataFolder, fileName);

            //remove this file as a candidate for pruning
            for (File cf : files) {
                if (cf.equals(f)) {
                    files.remove(cf);
                    break;
                }
            }

            if (!f.exists() || !new ByteArrayReader(f).getMD5Hash().equals(fileHash)) {
                filesToRequest.add(fileName);
            }
        }
        return filesToRequest;
    }
}
