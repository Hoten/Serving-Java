package hoten.serving;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

public class ByteArrayWriter {

    public static ServerConnectionHandler serverConnection;

    private final DataOutputStream dos;
    private final ByteArrayOutputStream baos;
    private int _type;

    public ByteArrayWriter(int initialSize) {
        baos = new ByteArrayOutputStream(initialSize);
        dos = new DataOutputStream(baos);
    }

    public ByteArrayWriter() {
        this(32);
    }

    public void writeFloat(float value) {
        try {
            dos.writeFloat(value);
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeInt(int value) {
        try {
            dos.writeInt(value);
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeShort(int value) {
        try {
            dos.writeShort(value);
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeByte(int value) {
        try {
            dos.writeByte(value);
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeBoolean(boolean value) {
        try {
            dos.writeBoolean(value);
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeUTF(String value) {
        try {
            dos.writeUTF(value);
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeUTFBytes(String value) {
        try {
            dos.write(value.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void compress() {
        byte[] bytes = toByteArray();
        baos.reset();

        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        compressor.setInput(bytes);
        compressor.finish();
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            baos.write(buf, 0, count);
        }
        try {
            baos.close();
        } catch (IOException ex) {
            Logger.getLogger(ByteArrayWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }

    public void send() {
        serverConnection.send(_type, toByteArray());
    }

    public void setType(int type) {
        _type = type;
    }

    public int getType() {
        return _type;
    }
}
