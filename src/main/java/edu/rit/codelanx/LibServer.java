package edu.rit.codelanx;

import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.cmd.text.TextRequest;
import edu.rit.codelanx.cmd.text.TextResponse;
import edu.rit.codelanx.data.DataFacade;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.ui.Client;
import edu.rit.codelanx.ui.IMessage;
import edu.rit.codelanx.ui.TextMessage;

import java.io.IOException;

public class LibServer implements Server {

    private final DataStorage storage; //stores data
    private final Clock clock; //passes the time
    private final Interpreter<String, TextMessage> commands;
    private Client client;

    /**
     * Starts our program, loads relevant data, begins ticking server logic, etc
     */
    public LibServer() {
        this.storage = new DataFacade();
        this.clock = new Clock(this);
        this.commands = new TextInterpreter(this);
    }

    @Override
    public DataStorage getDataStorage() {
        return this.storage;
    }


    /**
     * sends request to interpreter
     * @param client
     * @param message
     */
    @Override
    public void receive(Client client, TextMessage message) {
        this.commands.receive(client, message);
    }

    @Override
    public Interpreter<String, TextMessage> getInterpreter() {
        return this.commands;
    }

    @Override
    public void registerClient(Client client) {
        this.client= client;

    }


}
