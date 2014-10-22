package client;

import hoten.serving.FileUtils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat {

    private ConnectionToChatServerHandler _serverConnection;
    private String _username;

    public void startChat(ConnectionToChatServerHandler serverConnection) {
        _serverConnection = serverConnection;
        final Scanner s = new Scanner(System.in);
        _username = promptUsername(s);
        readWelcomeMesssage();

        _serverConnection.sendUsername(_username);

        Executors.newSingleThreadExecutor().execute(() -> {
            while (processChatInput(s.nextLine()));
            _serverConnection.close();
        });
    }

    public void announceNewUser(String username) {
        display(String.format("%s has connected to the chat. Say hello!", username));
    }

    public void global(String from, String msg) {
        display(String.format("%s: %s", from, msg));
    }

    public void whisper(String from, String msg) {
        display(String.format("%s whispers to you: %s", from, msg));
    }

    public void announceDisconnect(String username) {
        display(String.format("%s has left the chat.", username));
    }

    public void announce(String msg) {
        display(msg);
    }

    private String promptUsername(Scanner s) {
        System.out.print("Enter your username (no spaces): ");
        String username = s.next();
        s.nextLine();
        return username;
    }

    private boolean processChatInput(String input) {
        boolean continueChat = true;
        if (input.startsWith("/")) {
            String[] split = input.split(" ", 2);
            if (split.length != 2) {
                return true;
            }
            String to = split[0].substring(1);
            String msg = split[1];
            _serverConnection.sendWhisper(to, msg);
        } else if (input.equalsIgnoreCase("quit")) {
            _serverConnection.quit();
            continueChat = false;
        } else {
            _serverConnection.sendMessage(input);
        }
        return continueChat;
    }

    private void readWelcomeMesssage() {
        try {
            File f = new File(_serverConnection.localDataFolder, "welcome.txt");
            String welcome = new String(FileUtils.getFileBytes(f), "UTF-8");
            String formatted = String.format(welcome, _username);
            System.out.println("\n" + formatted + "\n");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void display(String msg) {
        System.out.println(msg);
    }
}
