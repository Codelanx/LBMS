package edu.rit.codelanx.ui;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.data.state.State;

import java.io.IOException;

public interface Client extends Displayable{
    public void connect(Server server);
    public void renderState(State state);
}
