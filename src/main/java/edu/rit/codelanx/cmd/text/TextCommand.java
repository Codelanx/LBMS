package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.ResponseFlag;

public abstract class TextCommand implements Command<ResponseFlag> {

    protected final Server server;

    public TextCommand(Server server) {
        this.server = server;
    }

}
