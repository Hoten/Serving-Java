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
            this.process = process;
            this.in = in;
            this.out = out;
        }

        public final Process process;
        public final BufferedReader in;
        public final BufferedWriter out;
    }

    static ProcessStreams server;
    static List<ProcessStreams> clients = new ArrayList();

    private static ProcessStreams makeProcess(ProcessBuilder builder) throws IOException {
        builder.directory().mkdirs();
        Process process = builder.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        return new ProcessStreams(process, in, out);
    }

    @BeforeClass
    public static void setUpClass() throws IOException, InterruptedException, ExecutionException {
        String serverJarPath = new File("Chat-Example/target/Chat-Example-1.0-SNAPSHOT-jar-with-dependencies.jar").getAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder()
                .redirectErrorStream(true);

        server = makeProcess(pb
                .directory(new File("ChatIT"))
                .command("java", "-cp", serverJarPath, "server.ServerDriver"));

        int numClients = 5;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                int clientsLeft = numClients;
                while (clientsLeft > 0) {
                    String line = server.in.readLine();
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
            ProcessStreams client = makeProcess(pb
                    .directory(new File("ChatIT/" + clientName))
                    .command("java", "-cp", serverJarPath, "client.ClientDriver"));
            clients.add(client);
            client.out.write(clientName + "\n");
            client.out.flush();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void tearDownClass() {
        server.process.destroyForcibly();
        clients.forEach(client -> {
            client.process.destroyForcibly();
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
        sender.out.write("Hello world!\n");
        sender.out.flush();
        assertTrue(clients.stream().skip(1).allMatch(client -> {
            return client.in.lines().anyMatch(line -> line.contains("Hello world!"));
        }));
    }

    @Test
    public void testWhisper() throws IOException {
        ProcessStreams sender = clients.get(0);
        ProcessStreams reciever = clients.get(1);
        sender.out.write("/client1 1v1 me bro\n");
        sender.out.flush();
        assertTrue(reciever.in.lines().anyMatch(line -> line.contains("1v1 me bro")));
    }
}
