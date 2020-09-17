package edu.rit.codelanx.ui;

import edu.rit.codelanx.cmd.text.TextResponse;
import edu.rit.codelanx.data.State;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class TextClient implements Client, AutoCloseable {

    private final InputStream input;
    private final PrintStream output;
    private final BufferedReader buffer;
    private final InputStreamReader reader;

    public TextClient(InputStream input, PrintStream output) {
        this.input = input;
        this.output = output;
        this.reader = new InputStreamReader(input);
        this.buffer = new BufferedReader(this.reader);
    }

    @Override
    public void display() {
        //TODO: Read input buffer
        //TODO: Print out anything which needs to be displayed, e.g. reports
    }

    @Override
    public void renderState(State state) {

    }

    @Override
    public void sendMessage(String message) {
        this.output.println(message);
    }

    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
    }
}
