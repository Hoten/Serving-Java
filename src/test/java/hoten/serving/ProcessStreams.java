package hoten.serving;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ProcessStreams {

    public static ProcessStreams makeProcess(ProcessBuilder builder) throws IOException {
        builder.directory().mkdirs();
        Process process = builder.start();
        BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter in = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        return new ProcessStreams(process, out, in);
    }

    public ProcessStreams(Process process, BufferedReader out, BufferedWriter in) {
        _process = process;
        _out = out;
        _in = in;
    }

    final private Process _process;
    final private BufferedReader _out;
    final private BufferedWriter _in;

    public void writeAndFlush(String str) throws IOException {
        _in.write(str);
        _in.flush();
    }

    public void clearOutput() throws IOException {
        while (_out.ready()) {
            _out.readLine();
        }
    }

    public String readAll() throws IOException {
        StringBuilder builder = new StringBuilder();
        while (_out.ready()) {
            builder.append(_out.readLine()).append("\n");
        }
        return builder.toString();
    }

    private String readLine(int timeToWait) throws IOException, InterruptedException {
        if (!_out.ready()) {
            int numChecks = 20;
            for (int i = 0; i < numChecks && !_out.ready(); i++) {
                Thread.sleep(timeToWait / numChecks);
            }
        }
        if (!_out.ready()) {
            throw new IOException("Reading input took too long. Perhaps the stream isn't behaving as expected?");
        }
        return _out.readLine();
    }

    public String readLine() throws IOException, InterruptedException {
        return readLine(2000);
    }

    public void end() {
        _process.destroyForcibly();
    }
}
