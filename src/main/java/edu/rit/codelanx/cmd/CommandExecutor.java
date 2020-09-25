package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.state.State;

public interface CommandExecutor {

    public void sendMessage(String message);
    public void renderState(State... state);
}
