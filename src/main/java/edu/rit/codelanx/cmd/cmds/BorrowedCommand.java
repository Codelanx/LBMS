package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.ui.Client;

/**
 * Queries for a list of books currently borrowed by a specific visitor.
 * <p>
 * Request Format: borrowed,visitor ID
 * visitor ID is the unique 10-digit ID of the visitor.
 */
public class BorrowedCommand extends TextCommand {

    /**
     * Constructor for the BorrowedCommand class
     *
     * @param server the server that the command is to be run on
     */
    public BorrowedCommand(Server server) {
        super(server);
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "borrowed";
    }

    /**
     * Whenever this command is called, it will query the database for books
     * being currently borrowed by a visitor.
     *
     * @param executor  the client that is calling the command
     * @param arguments advance: name of the command to be run
     *                  visitorID: the id of the visitor to check
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(Client executor, String... arguments) {
        return ResponseFlag.NOT_FINISHED;
    }
}
