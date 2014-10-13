package hoten.serving;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    private FileUtils() {
    }

    public static List<File> getAllFilesInDirectory(File dir) {
        List<File> result = new ArrayList();
        for (File cur : dir.listFiles()) {
            if (cur.isDirectory()) {
                result.addAll(getAllFilesInDirectory(cur));
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
        loc.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(loc); DataOutputStream dos = new DataOutputStream(fos)) {
            loc.createNewFile();
            dos.write(bytes);
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}