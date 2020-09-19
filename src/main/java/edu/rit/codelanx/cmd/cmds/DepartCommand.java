package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.ui.Client;

/**
 * Ends a visit in progress.
 *
 * Request Format: depart,visitor ID
 * visitor ID is the unique 10-digit ID of the visitor
 */
public class DepartCommand extends TextCommand {

    /**
     * Constructor for the DepartCommand class
     *
     * @param server the server that the command is to be run on
     */
    public DepartCommand(Server server) {
        super(server);
    }

    /**
     * TODO: Figure out linking to Command
     */
    @Override
    public String getName() {
        return "depart";
    }

    /**
     * Whenever this command is called, it will end the visit of the visitor
     * whose id is specified.
     *
     * @param executor  the client that is calling the command
     * @param arguments depart: name of the command to be run
     *                  visitorID: the visitor whose visit will end
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(Client executor, String... arguments) {
        return null;
    }
}
