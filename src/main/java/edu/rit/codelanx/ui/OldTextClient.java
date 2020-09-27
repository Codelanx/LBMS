package edu.rit.codelanx.ui;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;


public class OldTextClient implements OldITextClient {
    private InputStreamReader reader;
    private BufferedReader buffer;
    private PrintStream output;
    private Server server;

    public OldTextClient(InputStream input, PrintStream output) {
        this.reader = new InputStreamReader(input);
        this.buffer = new BufferedReader(this.reader);
        this.output = output;
    }

    /**
     * reads from buffered reader then sends request to the server
     *
     * @throws IOException
     */
    @Override
    public void display() throws IOException {
        String str;
        while ((str = buffer.readLine()) != null) {
            //server.receive(this, new TextMessage(str));
        }
    }

    @Override
    public void connect(Server server) {
        //server.registerClient(this);
        this.server = server;
    }

    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
    }

    /**
     * renders infos of the specified state.
     *
     * @param state
     */
    @Override
    public void renderState(State state) {
        //see: network.client.Client#renderState
    }

    /**
     * format time (type instant) into a string
     *
     * @param time
     * @return
     */
    private String formatTime(Instant time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.US).withZone(ZoneId.systemDefault());
        String str_time = formatter.format(time);
        return str_time;
    }

    /**
     * receives message from the server.
     *
     * @param server
     * @param message
     */
    @Override
    public void receive(Server server, TextMessage message) throws IOException {
        this.output.println(message.getData());
    }
}
