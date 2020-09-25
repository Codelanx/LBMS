package edu.rit.codelanx.network.server;

import edu.rit.codelanx.network.client.Client;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.data.DataFacade;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.ITEMPDataStorage;
import edu.rit.codelanx.network.io.Message;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.MessengerAdapter;
import edu.rit.codelanx.network.io.TextMessage;

public class LibServer implements Server<TextMessage> {

    private final DataStorage storage; //stores data
    private final Clock clock; //passes the time
    private final Interpreter commands;
    private final MessengerAdapter<? super TextMessage> input;

    /**
     * Starts our program, loads relevant data, begins ticking server logic, etc
     */
    public LibServer() {
        this.storage = new DataFacade();
        this.clock = new Clock(this);
        this.commands = new TextInterpreter(this);
        this.input = new MessengerAdapter<>();
        this.input.onMessage(TextMessage.class, (from, msg) -> {
            if (!(from instanceof CommandExecutor)) {
                throw new IllegalArgumentException("Cannot receive messages from a non-CommandExecutor");
            }
            String data = msg.getData();
            this.getInterpreter().receive((CommandExecutor) from, data);
        });
    }

    @Override
    public Interpreter getInterpreter() {
        return this.commands;
    }

    @Override
    public ITEMPDataStorage getDataStorage() {
        return (ITEMPDataStorage) new Object(); //CCE, but better than NPE since that throws warnings -everywhere-
        //return this.storage; //TODO Fix this to return normally, once SpecialCommandMethods is resolved
    }

    @Override
    public Clock getClock() {
        return this.clock;
    }

    @Override
    public void registerClient(Client<TextMessage> client) {

    }

    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {
        if (from instanceof CommandExecutor) {
            this.getInterpreter().receive((CommandExecutor) from, message.getData());
        }

    }
}
