package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.ui.Client;

/**
 * Borrows a book for a visitor; uses the ID of a specific book or books returned in the most recent library book search.
 * <p>
 * Request Format: borrow,visitor ID,{id};
 * visitor ID is the unique 10-digit ID of the visitor.
 * id is the comma-separated list of IDs for the books to be borrowed by the visitor.
 */
public class BorrowCommand extends TextCommand {

    /**
     * Constructor for the BorrowCommand class
     *
     * @param server the server that the command is to be run on
     */
    public BorrowCommand(Server server) {
        super(server);
    }

    /**
     * TODO: Figure out linking to Command
     */
    @Override
    public String getName() {
        return "borrow";
    }

    /**
     * Whenever this command is called, it will borrow a book for a visitor.
     *
     * @param executor  the client that is calling the command
     * @param arguments borrow: name of the command to be run
     *                  visitorID: unique 10-digit ID of the visitor
     *                  id: comma-separated list of IDs for the books to be borrowed by the visitor
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(Client executor, String... arguments) {
        return null;
    }
}
