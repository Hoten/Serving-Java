package hoten.serving;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    private FileUtils() {
    }

    //todo: refactor
    public static List<File> getAllFilesInDirectory(File dir) {
        List<File> result = new ArrayList();
        Stack<File> all = new Stack();
        all.addAll(Arrays.asList(dir.listFiles()));
        while (!all.isEmpty()) {
            File cur = all.pop();
            if (cur.isDirectory()) {
                all.addAll(Arrays.asList(cur.listFiles()));
            } else {
                result.add(cur);
            }
        }
        return result;
    }
    
    public static void saveAs(File loc, ByteArrayWriter writer) {
        saveAs(loc, writer.toByteArray());
    }

    public static void saveAs(File loc, byte[] bytes) {
        try {
            FileOutputStream fos;
            DataOutputStream dos;
            loc.getParentFile().mkdirs();
            loc.createNewFile();
            fos = new FileOutputStream(loc);
            dos = new DataOutputStream(fos);
            dos.write(bytes);
            dos.close();
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
