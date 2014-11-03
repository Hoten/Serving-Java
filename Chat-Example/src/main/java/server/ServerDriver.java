package server;

import hoten.serving.fileutils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ServerDriver {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 1234;
        String clientDataDirName = "clientdata";
        String localDataDirName = "localdata";
        createRandomWelcomeMessage(clientDataDirName);
        ServingChat server = new ServingChat(port, clientDataDirName, localDataDirName);
        server.startServer();
        System.out.println("Server started.");
    }

    private static void createRandomWelcomeMessage(String clientDataDirName) {
        Date today = new Date(System.currentTimeMillis());
        double random = Math.random();
        String msg = String.format("Hello %%s! Welcome to the chat. Here is a random number: %f"
                + "\nAnd this is when the server was started: %s", random, today);
        FileUtils.saveAs(new File(clientDataDirName, "welcome.txt"), msg.getBytes());
    }
}
