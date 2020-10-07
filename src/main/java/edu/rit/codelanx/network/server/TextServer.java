package edu.rit.codelanx.network.server;

import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.data.DataFacade;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;

public class TextServer implements Server<TextMessage> {

    private final DataStorage storage; //stores data
    private final Clock clock; //passes the time
    private final Interpreter commands;

    /**
     * Starts our program, loads relevant data, begins ticking server logic, etc
     */
    public TextServer() {
        this.storage = new DataFacade();
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
