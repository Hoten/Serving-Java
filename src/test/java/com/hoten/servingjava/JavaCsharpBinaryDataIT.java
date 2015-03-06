package hoten.serving;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Test;

public class JavaCsharpBinaryDataIT {

    private final String csharpExePath = new File("src\\test\\csharp\\JavaCsharpBinaryDataIT\\bin\\Release\\JavaCsharpBinaryDataIT").getAbsolutePath();

    private ProcessStreams startClient() throws IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File("src"))
                .command(csharpExePath)
                .redirectErrorStream(true);
        return ProcessStreams.makeProcess(builder);
    }

    @Test
    public void testDataTransfers() throws IOException {
        try (ServerSocket server = new ServerSocket(1234)) {
            ProcessStreams client = startClient();
            Socket clientSocket = server.accept();

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            char[] bytes = new char[2000];
            Arrays.fill(bytes, 'a');
            String longString = new String(bytes);

            out.writeInt(123456);
            out.writeShort(1234);
            out.write(123);
            out.writeUTF("Hello World.");
            out.writeUTF(longString);

            assertEquals(123456, in.readInt());
            assertEquals(1234, in.readShort());
            assertEquals(123, in.read());
            assertEquals("Hello World.", in.readUTF());
            assertEquals(longString, in.readUTF());

            client.end();
        }
    }
}
