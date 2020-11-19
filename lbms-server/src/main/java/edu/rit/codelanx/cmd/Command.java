//LBMS Client Request Format: http://www.se.rit.edu/~swen-262/projects/design_project/ProjectDescription/LBMS-client-request-format.html
package edu.rit.codelanx.cmd;

/**
 * Represents an executable unit of code, which accepts arguments that modify
 * its executable behavior
 *
 * @author sja9291  Spencer Alderman
 * @author maa1675  Mark Anderson       some javadocs
 */
public interface Command {

    /**
     * The name of the command, aka the first argument passed through
     * the {@link Interpreter}
     *
     * @return the name of the command as a {@link String}
     */
    public String getName();

    /**
     * Returns the parameter list / full usage of the command
     *
     * @return The full usage string, excluding the {@link #getName} parameter
     */
    public String getUsage();

    /**
     * Executes this {@link Command}, running the contained code within
     * the command according to whoever implemented it
     *
     * @param executor  The {@link CommandExecutor} who called this command
     * @param args      The string arguments for the executed command
     * @return A {@link ResponseFlag} corresponding to the result of executing
     *         this command
     */
    public ResponseFlag onExecute(CommandExecutor executor, String... args);
}
