package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.state.State;

/**
 * The object which executed a particular command, providing an
 * interface for {@link Command} objects to refer back to and interact with
 * the given executor
 *
 * @author sja9291  Spencer Alderman
 */
public interface CommandExecutor {

    /**
     * Sends a string message to this {@link CommandExecutor}
     *
     * @param message The message to be sent
     */
    public void sendMessage(String message);

    /**
     * Sends the resultant {@link State} objects from an executed command
     * to be displayed to the executor, if it has a better way to display
     * than through {@link #sendMessage(String)}
     *
     * @param states The {@link State states} to be rendered
     */
    public void renderState(State... states);
}
