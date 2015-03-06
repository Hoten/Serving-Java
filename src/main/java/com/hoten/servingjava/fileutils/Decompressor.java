package com.hoten.servingjava.fileutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class Decompressor {

    public byte[] uncompress(byte[] data)  {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data))) {
            int numRead;
            byte[] buffer = new byte[2048];
            while((numRead = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, numRead);
            }   out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Decompressor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out.toByteArray();
    }
}
