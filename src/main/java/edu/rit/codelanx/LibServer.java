package edu.rit.codelanx;

import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.cmd.text.TextRequest;
import edu.rit.codelanx.cmd.text.TextResponse;
import edu.rit.codelanx.data.DataFacade;
import edu.rit.codelanx.data.DataStorage;

import java.io.IOException;

public class LibServer implements Server {

    private final DataStorage storage; //stores data
    private final Clock clock; //passes the time
    private final Interpreter<TextRequest, TextResponse> commands;

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

}
