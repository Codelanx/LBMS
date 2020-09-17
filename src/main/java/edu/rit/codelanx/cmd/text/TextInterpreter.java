package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.CommandMap;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.ui.Client;

public class TextInterpreter implements Interpreter<TextRequest, TextResponse> {

    private final Server server;

    public TextInterpreter(Server server) {
        this.server = server;
        CommandMap.initialize(server); //Enables commands on this server
    }

    @Override
    public TextResponse receive(Client executor, TextRequest request) {
        //TODO: Handle receiving a request here
        //was it terminated? if so, exec a command
        return null;
    }
}
