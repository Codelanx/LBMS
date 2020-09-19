package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.ui.Client;

public class ArriveCommand extends TextCommand {
    public ArriveCommand(Server server) {
        super(server);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ResponseFlag onExecute(Server ranOn, Client executor, String... arguments) {
        return null;
    }
}
