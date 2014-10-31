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
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        return new ProcessStreams(process, in, out);
    }
    
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
