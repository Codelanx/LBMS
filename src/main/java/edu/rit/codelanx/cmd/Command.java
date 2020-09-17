package edu.rit.codelanx.cmd;

import edu.rit.codelanx.cmd.text.TextResponse;
import edu.rit.codelanx.ui.Client;

public interface Command<R extends Response> {

    public String getName();
    public Response onExecute(Client<? extends TextResponse> executor, String... arguments);
}
