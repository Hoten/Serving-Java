package hoten.serving;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * ByteArray.java
 *
 * Allows for easy reading and writing of data.
 *
 * For writing:
 *
 * For best performance, pass to the constructor an integer estimate of the
 * maximum expected size. However, not necessary. When getBytes() is called, the
 * internal byte array will be trimmed at the current position, thus trimming
 * the excess buffer. If the position becomes greater than the buffer
 * (bytes.length), then it is increased by a factor of (1 + loadFactor).
 *
 * For reading:
 *
 * Be sure to call rewind() before beginning any data processing.
 *
 * IN GENERAL, do not mix writing and reading with the same ByteArray object.
 * Call new ByteArray(writer.getBytes()) if for some reason you want to read
 * data you just wrote. ByteArray is meant for sending data over a network, so
 * there shouldn't be any need to do this.
 *
 * @author Hoten
 */
public class ByteArray {

    final public static double loadFactor = .75f; //0 < x <= 1
    final public static int defaultInitialSize = 100;
    public static SocketHandler server;
    private byte[] bytes;
    private int pos;
    private int type = -1;

    public ByteArray() {
        bytes = new byte[defaultInitialSize];
    }

    public ByteArray(int initialSize) {
        if (initialSize <= 1) {
            initialSize = 2;
        }
        bytes = new byte[initialSize];
    }

    public ByteArray(byte[] initialBytes) {
        if (initialBytes != null) {
            bytes = initialBytes;
            pos = bytes.length;
        } else {
            throw new IllegalArgumentException("Invalid initial byte array");
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    //set the position to the begining. use before reading data
    public void rewind() {
        pos = 0;
    }

    //makes sure there is no excess data, and returns the byte array
    public byte[] getBytes() {
        trim();
        return bytes;
    }

    public int getSize() {
        return bytes.length;
    }

    //if this is acting as a reader, returns the amount of data left to be read.
    //if this is acting as a writer, returns the amount of buffer space left
    public int getBytesAvailable() {
        return bytes.length - pos;
    }

    //read methods
    public byte[] readBytes(int l) {
        byte[] b = new byte[l];
        for (int i = 0; i < l; i++) {
            b[i] = (byte) readByte();
        }
        return b;
    }

    public String readUTF() {
        int length = readShort();
        byte[] utf_bytes = readBytes(length);
        return new String(utf_bytes, 0, length);
    }

    public byte readByte() {
        return bytes[pos++];
    }

    public short readShort() {
        return (short) ((readByte() << 8) + (readByte() & 0xff));
    }

    public int readInt() {
        return (readByte() << 24) + ((readByte() & 0xff) << 16) + ((readByte() & 0xff) << 8) + (readByte() & 0xff);
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public boolean readBoolean() {
        return readByte() != 0;
    }

    //takes entirity of the byte array, uncompresses it, and then rewinds it.
    public void uncompress() {
        Inflater decompressor = new Inflater();
        decompressor.setInput(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
        }
        bytes = bos.toByteArray();
        pos = 0;
    }

    //cut off all the remaining buffer after the current position
    public void trim() {
        trim(pos);
    }

    //TODO would this EVER need to be public? any use for it besides trim(pos)?
    public void trim(int at) {
        if (at < bytes.length) {
            byte[] b = new byte[at];
            System.arraycopy(bytes, 0, b, 0, b.length);
            bytes = b;
        }
    }

    //increase the size of the internal byte array
    private void expand() {
        int newSize = (int) Math.ceil(bytes.length * (loadFactor + 1));
        byte[] newByteArr = new byte[newSize];
        System.arraycopy(bytes, 0, newByteArr, 0, bytes.length);
        bytes = newByteArr;
    }

    //write methods
    public void writeFloat(float fv) {
        int v = Float.floatToIntBits(fv);
        writeInt(v);
    }

    public void writeInt(int v) {
        writeByte(v >> 24);
        writeByte(v >> 16);
        writeByte(v >> 8);
        writeByte(v);
    }

    public void writeShort(int v) {
        writeByte(v >> 8);
        writeByte(v);
    }

    public void writeByte(int v) {
        if (bytes.length <= pos) {
            expand();
        }
        bytes[pos++] = (byte) v;
    }

    public void writeBytes(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            writeByte(b[i]);
        }
    }

    public void writeBoolean(boolean b) {
        writeByte(b ? 1 : 0);
    }

    public void writeUTF(String str) {
        writeShort((short) str.length());
        writeUTFBytes(str);
    }

    public void writeUTFBytes(String str) {
        byte[] strArr = null;
        try {
            strArr = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ByteArray.class.getName()).log(Level.SEVERE, null, ex);
        }
        writeBytes(strArr);
    }

    //trims off the buffer, then compresses and sets pos to the end.
    public void compress() {
        trim();
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        compressor.setInput(bytes);
        compressor.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
        }
        bytes = bos.toByteArray();
        pos = bytes.length;
    }
    
    //only for use client-side
    public void send() {
        server.send(this);
    }
}
