package edu.rit.codelanx.network.client;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.network.io.Message;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Represents a {@link Client} ran through the terminal
 *
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 *
 * @see Client
 */
public class TextClient implements Client<TextMessage> {

/*
TODO:

Console output:

Add a prompt (e.g.):
        $>> input
        output
        output
        $>> input2
        output
        output
 */

    private static final String PROMPT_PREFIX = "$-> ";
    private final InputStreamReader reader;
    private final BufferedReader buffer;
    private final PrintStream output;
    private volatile WeakReference<Server<TextMessage>> server;

    /**
     * constructs the interactive terminal based-session for users
     * @param input- from the client
     * @param output- to the client
     */
    public TextClient(InputStream input, PrintStream output) {
        this.reader = new InputStreamReader(input);
        this.buffer = new BufferedReader(this.reader);
        this.output = output;
    }

    /**
     * {@inheritDoc}
     *
     * @param server {@inheritDoc}
     *
     * @see Client#connect(Server)
     */
    @Override
    public void connect(Server<TextMessage> server) {
        this.server = new WeakReference<>(server);
    }

    /**
     * Begins reading input from the terminal, until a {@code null} is read
     *
     * @throws IOException {@inheritDoc}
     * @see Client#display()
     */
    @Override
    public void display() throws IOException {
        if (this.output == System.out) {
            //clear the screen on start (print 100 blank lines)
            for (int i = 0; i < 100; i++) {
                this.output.println();
            }
        }
        this.output.print(PROMPT_PREFIX);
        String s;
        while ((s = this.buffer.readLine()) != null) {
            //System.out.println("$>>input: "); //TODO:
            Server<TextMessage> server = this.server.get();
            if (server == null) {
                System.err.println("Not connected to server!"); //prints per input attempt
                continue;
            }
            this.message(server, new TextMessage(s));
            this.output.print(PROMPT_PREFIX);
        }
    }

    /**
     * terminates the application when called
     * @throws Exception- when errors occur
     */
    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
    }

    /**
     * print out the message to user via terminal
     * @param message- to be printed out
     */
    @Override
    public void sendMessage(String message) {
        this.output.println(message);
    }

    /**
     * print out the user' states via terminal
     * @param states -array of states
     */
    @Override
    public void renderState(State... states) {
        Arrays.stream(states)
                .map(State::toFormattedText)
                .forEach(this.output::println); //final code version
    }

    /**
     * prints out the text message from a messenger to the terminal
     * @throws UnsupportedOperationException if text message come from a non-server messenger
     * @param from-The {@link Messenger} we received a message from
     * @param message-The received {@link Message}
     */
    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {
        if (!(from instanceof Server)) { //extra safety check, necessary?
            throw new UnsupportedOperationException("(Shouldn't) support messaging from non-servers");
        }
        this.output.println(message.getData());
    }

}
