//LBMS Client Request Format: http://www.se.rit.edu/~swen-262/projects/design_project/ProjectDescription/LBMS-client-request-format.html
package edu.rit.codelanx.cmd;

public interface Command {

    /**
     * Representation of the name of the command
     *
     * @return the name of the command as a string
     */
    public String getName();

    /**
     * Returns the parameter list / full usage of the command
     *
     * @return The full usage string, excluding the {@link #getName} parameter
     */
    public String getUsage();

    /**
     *
     * @param executor
     * @param args      The args for the executed command
     * @return
     */
    public ResponseFlag onExecute(CommandExecutor executor, String... args);
}
