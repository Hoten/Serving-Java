package hoten.serving;

import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ByteArrayTest {

    ByteArrayWriter _writer;

    @Before
    public void setUp() {
        _writer = new ByteArrayWriter();
    }

    private Random getRandom() {
        return new Random("ByteArray Test".hashCode());
    }

    @Test
    public void testByteArray() {
        Random r = getRandom();

        _writer.writeUTF("Hello World");
        for (int i = 0; i < 1000; i++) {
            _writer.writeInt(r.nextInt());
        }
        for (int i = 0; i < 1000; i++) {
            _writer.writeShort(r.nextInt() >> 16);
        }
        for (int i = 0; i < 1000; i++) {
            _writer.writeByte(r.nextInt() >> 24);
        }
        for (int i = 0; i < 1000; i++) {
            _writer.writeFloat(Float.intBitsToFloat(r.nextInt()));
        }
        for (int i = 0; i < 1000; i++) {
            _writer.writeBoolean(r.nextBoolean());
        }
        _writer.compress();

        r = getRandom();
        ByteArrayReader reader = new ByteArrayReader(_writer);
        reader.uncompress();

        assertEquals("Hello World", reader.readUTF());
        for (int i = 0; i < 1000; i++) {
            assertEquals(r.nextInt(), reader.readInt());
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals(r.nextInt() >> 16, reader.readShort());
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals(r.nextInt() >> 24, reader.readByte());
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals(Float.intBitsToFloat(r.nextInt()), reader.readFloat(), 0);
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals(r.nextBoolean(), reader.readBoolean());
        }
    }
}
