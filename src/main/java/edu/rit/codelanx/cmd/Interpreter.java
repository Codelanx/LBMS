package edu.rit.codelanx.cmd;

import edu.rit.codelanx.ui.Client;

public interface Interpreter<Q extends Request, R extends Response> {

    public R receive(Client<? extends R> executor, Q request);

}
