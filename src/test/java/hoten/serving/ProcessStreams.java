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

    public ProcessStreams(Process process, BufferedReader in, BufferedWriter out) {
        _process = process;
        _in = in;
        _out = out;
    }

    final private Process _process;
    final private BufferedReader _in;
    final private BufferedWriter _out;

    public void writeAndFlush(String str) throws IOException {
        _out.write(str);
        _out.flush();
    }

    public void clearOutput() throws IOException {
        while (_in.ready()) {
            _in.readLine();
        }
    }

    public String readAll() throws IOException {
        StringBuilder builder = new StringBuilder();
        while (_in.ready()) {
            builder.append(_in.readLine()).append("\n");
        }
        return builder.toString();
    }

    private String readLine(int timeToWait) throws IOException, InterruptedException {
        if (!_in.ready()) {
            int numChecks = 20;
            for (int i = 0; i < numChecks && !_in.ready(); i++) {
                Thread.sleep(timeToWait / numChecks);
            }
        }
        if (!_in.ready()) {
            throw new IOException("Reading input took too long. Perhaps the stream isn't behaving as expected?");
        }
        return _in.readLine();
    }

    public String readLine() throws IOException, InterruptedException {
        return readLine(2000);
    }

    public void end() {
        _process.destroyForcibly();
    }
}
