//LBMS Client Request Format: http://www.se.rit.edu/~swen-262/projects/design_project/ProjectDescription/LBMS-client-request-format.html
package edu.rit.codelanx.cmd;

public interface Command {

    /**
     * Representation of the name of the command
     *
     * @return the name of the command as a string
     */
    public String getName();

    public ResponseFlag onExecute(CommandExecutor executor, String... arguments);
}
