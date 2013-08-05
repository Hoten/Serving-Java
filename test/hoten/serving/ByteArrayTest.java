package hoten.serving;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Connor
 */
public class ByteArrayTest {

    public ByteArrayTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public ByteArray getTestByteArray() {
        ByteArray writer = new ByteArray();
        Random r = new Random("ByteArray Test".hashCode());

        writer.writeUTF("Hello World");
        for (int i = 0; i < 1000; i++) {
            writer.writeInt(r.nextInt());
        }
        for (int i = 0; i < 1000; i++) {
            writer.writeShort(r.nextInt() >> 16);
        }
        for (int i = 0; i < 1000; i++) {
            writer.writeByte(r.nextInt() >> 24);
        }
        for (int i = 0; i < 1000; i++) {
            writer.writeFloat(Float.intBitsToFloat(r.nextInt()));
        }
        for (int i = 0; i < 1000; i++) {
            writer.writeBoolean(r.nextBoolean());
        }

        writer.compress();

        return writer;
    }

    @Test
    public void testByteArray() {
        ByteArray reader = getTestByteArray();
        Random r = new Random("ByteArray Test".hashCode());
        reader.rewind();

        reader.uncompress();

        if (!reader.readUTF().equals("Hello World")) {
            fail();
        }
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
