package edu.rit.codelanx.cmd;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.ui.Client;

public interface Command<R extends Response> {

    public String getName();
    public R onExecute(Client executor, String... arguments);
}
