package hoten.serving;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ByteArrayReader {

    private DataInputStream dis;
    private int _type;
    private int _length;

    public ByteArrayReader(byte[] bytes) {
        createDataInputStream(bytes);
    }

    public ByteArrayReader(ByteArrayWriter writer) {
        createDataInputStream(writer.toByteArray());
    }

    public ByteArrayReader(File file) {
        try {
            dis = new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public float readFloat() {
        try {
            return dis.readFloat();
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error("Error in byte array reader.");
        }
    }

    public int readInt() {
        try {
            return dis.readInt();
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error("Error in byte array reader.");
        }
    }

    public int readShort() {
        try {
            return dis.readShort();
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error("Error in byte array reader.");
        }
    }

    public int readByte() {
        try {
            return dis.readByte();
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error("Error in byte array reader.");
        }
    }

    public boolean readBoolean() {
        try {
            return dis.readBoolean();
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error("Error in byte array reader.");
        }
    }

    public String readUTF() {
        try {
            return dis.readUTF();
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new Error("Error in byte array reader.");
        }
    }

    public String readUTFBytes(int length) {
        try {
            byte[] bytes = new byte[length];
            dis.readFully(bytes);
            return new String(bytes, 0, length);
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public byte[] toByteArray() {
        try {
            byte[] bytes = new byte[dis.available()];
            dis.readFully(bytes);
            return bytes;
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void uncompress() {
        try {
            byte[] bytes = toByteArray();
            Inflater decompressor = new Inflater();
            decompressor.setInput(bytes);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            bos.close();
            createDataInputStream(bos.toByteArray());
        } catch (IOException | DataFormatException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMD5Hash() {
        try {
            byte[] bytes = toByteArray();
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            byte[] messageDigest = algorithm.digest();
            StringBuilder hash = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                int num = 0xFF & messageDigest[i];
                String append = Integer.toHexString(num);
                if (append.length() == 1) {
                    append = "0" + append;
                }
                hash.append(append);
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ByteArrayReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void createDataInputStream(byte[] bytes) {
        dis = new DataInputStream(new ByteArrayInputStream(bytes));
    }

    public int getType() {
        return _type;
    }

    public void setType(int _type) {
        this._type = _type;
    }
}
