package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.types.Visitor;
import edu.rit.codelanx.ui.Client;

public class RegisterCommand implements Command<ResponseFlag> {

    private final Server server;

    public RegisterCommand(Server server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public ResponseFlag onExecute(Server ranOn, Client executor, String... arguments) {
        //TODO: Register a new Visitor with the DataStorage
        this.server.getDataStorage().add(new Visitor()); //TODO: Fill out visitor's data from args
        return ResponseFlag.SUCCESS;
    }
}
