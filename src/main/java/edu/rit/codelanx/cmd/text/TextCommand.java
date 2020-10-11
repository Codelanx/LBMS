package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;

public abstract class TextCommand implements Command {

    protected final Server<TextMessage> server;
    volatile String[] missingArgs; //biiiit of a hack

    public TextCommand(Server<TextMessage> server) {
        this.server = server;
    } //TODO: Handle server through the onExecute

    protected void setMissingArgs(String... args) {
        this.missingArgs = missingArgs;
    }
}
