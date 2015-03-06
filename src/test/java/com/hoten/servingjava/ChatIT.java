package hoten.serving;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class ChatIT {

    private static ProcessStreams server;
    private static final List<ProcessStreams> clients = new ArrayList();
    private static ProcessStreams aJavaClient;
    private static ProcessStreams aCsharpClient;
    private static final String jarPath = new File("Chat-Example/build/libs/Chat-Example-all.jar").getAbsolutePath();
    private static final String csharpExePath = new File("Chat-Example/src/main/csharp/ChatClient/ChatClient/bin/Release/ChatClient").getAbsolutePath();

    private static ProcessStreams makeServerProcess() throws IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File("ChatIT"))
                .command("java", "-cp", jarPath, "server.ServerDriver")
                .redirectErrorStream(true);
        return ProcessStreams.makeProcess(builder);
    }

    private static ProcessStreams makeJavaClientProcess(String clientName) throws IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File("ChatIT/" + clientName))
                .command("java", "-cp", jarPath, "client.ClientDriver")
                .redirectErrorStream(true);
        return ProcessStreams.makeProcess(builder);
    }

    private static ProcessStreams makeCsharpClientProcess(String clientName) throws IOException {
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File("ChatIT/" + clientName))
                .command(csharpExePath)
                .redirectErrorStream(true);
        return ProcessStreams.makeProcess(builder);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        server = makeServerProcess();
        server.readLine();

        int numClients = 5;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            int clientsLeft = numClients;
            while (clientsLeft > 0) {
                String line = "";
                try {
                    line = server.readLine();
                } catch (IOException | InterruptedException ex) {
                }
                if (line.contains("has joined")) {
                    clientsLeft--;
                    System.out.println("clientsLeft = " + clientsLeft);
                }
            }
        });

        for (int i = 0; i < numClients; i++) {
            final String clientName = "client" + i;
            ProcessStreams client = i % 2 == 0 ? makeJavaClientProcess(clientName) : makeCsharpClientProcess(clientName);
            clients.add(client);
            client.writeAndFlush(clientName + "\n");
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        aJavaClient = clients.get(0);
        aCsharpClient = clients.get(1);
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        server.end();
        clients.forEach(client -> {
            client.end();
        });
        FileUtils.deleteDirectory(new File("ChatIT"));
        System.out.println("===========\nServer output: \n===========\n" + server.readAll());
    }

    @Before
    public void before() throws IOException, InterruptedException {
        Thread.sleep(100);
        for (ProcessStreams client : clients) {
            client.clearOutput();
        }
    }

    @Test
    public void testWelcomeMessageWasDownloaded() throws IOException {
        assertTrue(Files.exists(Paths.get("ChatIT/client0/localdata/welcome.txt")));
        assertTrue(Files.exists(Paths.get("ChatIT/client1/localdata/welcome.txt")));
    }

    private void testSendingMessage(ProcessStreams sender) throws IOException {
        sender.writeAndFlush("Hello world!\n");
        clients.stream().filter(client -> client != sender).forEach(client -> {
            try {
                String line = client.readLine();
                assertThat(line, containsString("Hello world!"));
            } catch (IOException | InterruptedException ex) {
                fail("No output found.");
            }
        });
    }

    @Test
    public void testSendingMessage() throws IOException, InterruptedException {
        testSendingMessage(aJavaClient);
        before(); // needed?
        testSendingMessage(aCsharpClient);
    }

    public void testWhisper(ProcessStreams sender, ProcessStreams reciever, String recieverUsername) throws IOException, InterruptedException {
        sender.writeAndFlush(String.format("/%s 1v1 me bro\n", recieverUsername));
        assertThat(reciever.readLine(), containsString("1v1 me bro"));
    }

    @Test
    public void testWhisper() throws IOException, InterruptedException {
        testWhisper(aJavaClient, aCsharpClient, "client1");
        testWhisper(aCsharpClient, aJavaClient, "client0");
    }
}
