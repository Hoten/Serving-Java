package hoten.serving;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Decompressor {

    public byte[] uncompress(byte[] bytes) {
        try {
            Inflater decompressor = new Inflater();
            decompressor.setInput(bytes);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            bos.close();
            return bos.toByteArray();
        } catch (IOException | DataFormatException ex) {
            Logger.getLogger(Decompressor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
