package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.ui.Client;

public abstract class TextCommand implements Command<ResponseFlag> {

    private final String name;
    protected final Server server;

    public TextCommand(Server server, String name) {
        this.name = name;
        this.server = server;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
