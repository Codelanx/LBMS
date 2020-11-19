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
     * Flushes any buffered output to the backing source
     *
     * @return {@code true} if output was sent, {@code false} otherwise
     */
    public boolean flush();
}
