package edu.rit.codelanx.ui;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.network.server.Server;

public interface OIdClient extends OldDisplayable {
    public void connect(Server server);
    public void renderState(State state);
}
