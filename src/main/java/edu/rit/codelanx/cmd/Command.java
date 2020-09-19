//LBMS Client Request Format: http://www.se.rit.edu/~swen-262/projects/design_project/ProjectDescription/LBMS-client-request-format.html
package edu.rit.codelanx.cmd;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.ui.Client;

public interface Command<R extends Response> {

    /**
     * Representation of the name of the command
     *
     * @return the name of the command as a string
     */
    public String getName();

    public R onExecute(Client executor, String... arguments);
}
