package edu.rit.codelanx.ui;

import edu.rit.codelanx.cmd.Response;
import edu.rit.codelanx.data.State;

public interface Client<R extends Response> {

    public void display(); //runs the client, displaying it until the user exits
    public void renderState(State state);
    public void sendMessage(String message);
}
