package edu.rit.codelanx.cmd;

/**
 * Responsible for interpreting input from a client and responding to it,
 * using {@link Command} objects (aka the command pattern)
 *
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @author maa1675  Mark Anderson
 */
public interface Interpreter {

    /**
     * Parses the given {@code data}, executing a {@link Command} if necessary.
     * Otherwise, provides the appropriate feedback to {@code executor} about
     * their input {@code data}
     *
     * @param executor The {@link CommandExecutor} that sent this {@code data}
     * @param data The {@link String} of data we received
     */
    public void receive(CommandExecutor executor, String data);
}