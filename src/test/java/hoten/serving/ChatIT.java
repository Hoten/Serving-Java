package hoten.serving;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChatIT {

    private static class ProcessStreams {

        public ProcessStreams(Process process, BufferedReader in, BufferedWriter out) {
            _process = process;
            _in = in;
            _out = out;
        }

        final Process _process;
        final BufferedReader _in;
        final BufferedWriter _out;

        public void writeAndFlush(String str) throws IOException {
            _out.write(str);
            _out.flush();
        }

        public String readLine() throws IOException {
            return _in.readLine();
        }

        public void end() {
            _process.destroyForcibly();
        }
    }

    static ProcessStreams server;
    static List<ProcessStreams> clients = new ArrayList();
    static String jarPath = new File("Chat-Example/target/Chat-Example-1.0-SNAPSHOT-jar-with-dependencies.jar").getAbsolutePath();
    static String csharpExePath = new File("Chat-Example/src/main/csharp/ChatClient/ChatClient/bin/Release/ChatClient").getAbsolutePath();

    private static ProcessStreams makeProcess(ProcessBuilder builder) throws IOException {
        builder.directory().mkdirs();
        Process process = builder.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        return new ProcessStreams(process, in, out);
    }

    private static ProcessStreams makeServerProcess() throws IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File("ChatIT"))
                .command("java", "-cp", jarPath, "server.ServerDriver")
                .redirectErrorStream(true);
        return makeProcess(builder);
    }

    private static ProcessStreams makeJavaClientProcess(String clientName) throws IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File("ChatIT/" + clientName))
                .command("java", "-cp", jarPath, "client.ClientDriver")
                .redirectErrorStream(true);
        return makeProcess(builder);
    }

    private static ProcessStreams makeCsharpClientProcess(String clientName) throws IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File("ChatIT/" + clientName))
                .command(csharpExePath)
                .redirectErrorStream(true);
        return makeProcess(builder);
    }

    @BeforeClass
    public static void setUpClass() throws IOException, InterruptedException, ExecutionException {
        server = makeServerProcess();

        int numClients = 5;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                int clientsLeft = numClients;
                while (clientsLeft > 0) {
                    String line = server._in.readLine();
                    if (line.contains("has joined")) {
                        clientsLeft--;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ChatIT.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        for (int i = 0; i < numClients; i++) {
            final String clientName = "client" + i;
            ProcessStreams client = i % 2 == 0 ? makeJavaClientProcess(clientName) : makeCsharpClientProcess(clientName);
            clients.add(client);
            client.writeAndFlush(clientName + "\n");
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void tearDownClass() {
        server.end();
        clients.forEach(client -> {
            client.end();
        });
        FileUtils.deleteRecursive(new File("ChatIT"));
    }

    @Test
    public void testWelcomeMessage() throws IOException {
        assertTrue(Files.exists(Paths.get("ChatIT/client0/localdata/welcome.txt")));
    }

    @Test
    public void testSendingMessage() throws IOException {
        ProcessStreams sender = clients.get(0);
        sender.writeAndFlush("Hello world!\n");
        assertTrue(clients.stream().skip(1).allMatch(client -> {
            return client._in.lines().anyMatch(line -> line.contains("Hello world!"));
        }));
    }

    @Test
    public void testWhisper() throws IOException {
        ProcessStreams sender = clients.get(0);
        ProcessStreams reciever = clients.get(1);
        sender.writeAndFlush("/client1 1v1 me bro\n");
        assertTrue(reciever._in.lines().anyMatch(line -> line.contains("1v1 me bro")));
    }
}
