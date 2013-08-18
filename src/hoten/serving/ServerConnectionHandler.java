package hoten.serving;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ServerConnectionHandler.java
 *
 * Extend this class to handle data from the server client-side. See chat
 * example for more details.
 *
 * @author Hoten
 */
public abstract class ServerConnectionHandler extends SocketHandler {

    private File localDataFolder;

    public ServerConnectionHandler(Socket socket) throws IOException {
        super(socket);
        startReadingThread();
    }

    @Override
    final void startReadingThread() {
        new Thread("handle data for: " + socket.getInetAddress()) {
            @Override
            public void run() {
                try {
                    String localDataFolderName = in.readUTF();
                    localDataFolder = new File(localDataFolderName);
                    processFileUpdatesClientside();
                    onConnectionSettled();
                    processDataUntilClosed();
                } catch (IOException ex) {
                    Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
                    close();
                }
            }
        }.start();
    }

    private void processFileUpdatesClientside() throws IOException {
        int numFiles = respondToHashes();
        for (int i = 0; i < numFiles; i++) {
            String fileName = in.readUTF();
            int len = in.readInt();
            byte[] b = new byte[len];
            in.read(b);
            System.out.println("Updating " + fileName + ", size = " + len);
            ByteArray.saveAs(new File(localDataFolder, fileName), b);
        }
        out.write(0);//done updating files
    }

    /**
     *
     * reads hashes sent from the server and sends file update requests if
     * server hash != local hash
     *
     * if the server hash != the client's local hash, the file does not exist,
     * or if there exists files/folders in the localdata folder that the server
     * does not explicitly list....then delete the excess files/folders
     *
     * returns number of files that the client has requested
     *
     * also prunes the client's localdata of files/folders that are not in the
     * server's clientdata folder
     *
     */
    private int respondToHashes() throws IOException {
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
        int numFiles = in.readInt();
        ArrayList<String> fnames = new ArrayList();
        for (int i = 0; i < numFiles; i++) {
            String fileName = in.readUTF();
            String fileHash = in.readUTF();
            File f = new File(localDataFolder, fileName);

            //remove this file as a candidate for pruning
            for (File cf : currentf) {
                if (cf.equals(f)) {
                    currentf.remove(cf);
                    break;
                }
            }

            //if locally the file doesn't exist or the hash is wrong, request update
            if (!f.exists() || !ByteArray.readFromFile(f).getMD5Hash().equals(fileHash)) {
                fnames.add(fileName);
            }
        }

        //write requests to stream
        out.writeInt(fnames.size());
        for (String fname : fnames) {
            out.writeUTF(fname);
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

        return fnames.size();
    }
}
