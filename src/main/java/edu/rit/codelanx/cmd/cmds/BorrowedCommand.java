package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.types.Visitor;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static java.lang.Long.parseLong;

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
    public ResponseFlag onExecute(CommandExecutor executor, String... arguments) {
        //Checking that the amount of arguments is correct
        if (arguments.length != 1) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID");
            return ResponseFlag.SUCCESS;
        }

        long visitorID;
        //Checking that the id passed was a number
        try {
            visitorID = parseLong(arguments[0]);
        } catch (NumberFormatException e) {
            return ResponseFlag.FAILURE;
        }

        //Finding the visitor with the matching ID in the database
        Optional<? extends Visitor> visitorSearch = this.server.getDataStorage()
                .ofLoaded(Visitor.class)
                .filter(v -> v.getID() == visitorID)
                .findAny();

        //Seeing if the search found a visitor
        Visitor visitor;
        try {
            visitor = visitorSearch.get();
        } catch (NoSuchElementException e) {
            executor.sendMessage(this.getName() + ",invalid-visitor-id;");
            return ResponseFlag.SUCCESS;
        }

        //TODO: Find the books currently being borrowed by the visitor

        return ResponseFlag.NOT_FINISHED;
    }
}
