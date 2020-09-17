package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.cmds.RegisterCommand;
import edu.rit.codelanx.ui.Client;

import java.util.HashMap;
import java.util.Map;

public class TextInterpreter implements Interpreter<TextRequest, TextResponse> {

    private final Map<String, Command<? extends TextResponse>> commands = new HashMap<>();
    private final Server server;

    public TextInterpreter(Server server) {
        this.server = server;
    }

    private void registerDefaults() {
        //register default commands here, e.g.
        this.addCommand(new RegisterCommand(this.server));
    }

    private void addCommand(Command<TextResponse> command) {
        this.commands.put(command.getName(), command);
    }

    @Override
    public TextResponse receive(Client<? extends TextResponse> executor, TextRequest request) {
        //TODO: Handle receiving a request here
        //was it terminated? if so, exec a command
        return null;
    }
}
