package edu.rit.codelanx.network.client.text;

import edu.rit.codelanx.network.client.Client;
import edu.rit.codelanx.network.io.Message;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.util.Errors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.stream.IntStream;

/**
 * Represents a {@link Client} ran through the terminal
 *
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see Client
 */
public class TextClient implements Client<TextMessage> {

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
            IntStream.range(0, 100) //a Stream<Integer> from 0 to 99
                .forEach(i -> this.output.println()); //print a line for each
        }
        for (;;) { //loop infinitely
            this.output.print(PROMPT_PREFIX); //print our command prompt
            String line = this.buffer.readLine(); //grab the user's input
            if (line == null) break; //EOF / no more input
            Server<TextMessage> server = this.server.get(); //Get our connected server
            if (server == null) { //if not connected, discard this input
                this.output.println("Error: Not connected to server!");
                continue;
            }
            try {
                this.sendTo(server, new TextMessage(line)); //otherwise, send it off
            } catch (Throwable t) {
                this.output.println("Server encountered error while processing latest request");
                if (this.output == System.out) {
                    Errors.report(t, this.output);
                } else {
                    Errors.report(t);
                    System.err.flush();
                }
            }
        };
    }

    /**
     * Terminates the application when called, freeing the buffers given to us
     * initially
     *
     * @throws Exception {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
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
