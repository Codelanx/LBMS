package edu.rit.codelanx.cmd;

import edu.rit.codelanx.ui.Client;
import edu.rit.codelanx.ui.IMessage;
import edu.rit.codelanx.ui.TextMessage;

public interface Interpreter<Q extends String, R extends IMessage> {

    public void receive(Client executor, R request);

    //TODO: Handle partial request
}
