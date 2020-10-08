package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.state.State;

/**
 * This provides interface to execute a particular command
 * @author sja9291  Spencer Alderman
 */
public interface CommandExecutor {
    /**
     * notify user about a message
     * @param message-a string to be printed out.
     */
    public void sendMessage(String message);

    /**
     * gets the current state of the user
     * @param state-{@link State}
     */
    public void renderState(State... state);
}
