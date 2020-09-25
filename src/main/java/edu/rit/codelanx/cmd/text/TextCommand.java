package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;

public abstract class TextCommand implements Command {

    protected final Server<TextMessage> server;

    public TextCommand(Server<TextMessage> server) {
        this.server = server;
    } //TODO: Handle server through the onExecute

}
