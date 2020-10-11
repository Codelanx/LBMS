package edu.rit.codelanx.network.server;

import edu.rit.codelanx.data.loader.BookStoreAdapter;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.data.LibraryData;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;

public class TextServer implements Server<TextMessage> {

    private final DataStorage storage; //stores data
    private final DataStorage bookStore;
    private final Clock clock; //passes the time
    private final Interpreter commands;

    /**
     * Starts our program, loads relevant data, begins ticking server logic, etc
     */
    public TextServer() {
        this.bookStore = new LibraryData(BookStoreAdapter::new);
        this.storage = new LibraryData();
        this.clock = new Clock(this);
        this.commands = new TextInterpreter(this);
    }

    @Override
    public Interpreter getInterpreter() {
        return this.commands;
    }

    @Override
    public DataStorage getDataStorage() {
        return this.storage;
    }

    @Override
    public DataStorage getBookStore() {
        return this.bookStore;
    }

    @Override
    public Clock getClock() {
        return this.clock;
    }

    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {
        if (!(from instanceof CommandExecutor)) {
            throw new IllegalArgumentException("Cannot receive messages from a non-CommandExecutor");
        }
        String data = message.getData();
        this.getInterpreter().receive((CommandExecutor) from, data);
    }
}
