package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import static java.lang.Long.parseLong;

/**
 * Borrows a book for a visitor; uses the ID of a specific book or books
 * returned in the most recent library book search.
 * <p>
 * Request Format: borrow,visitor ID,{id};
 * visitor ID is the unique 10-digit ID of the visitor.
 * id is the comma-separated list of IDs for the books to be borrowed by the
 * visitor.
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
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "borrow";
    }

    /**
     * Whenever this command is called, it will borrow a book for a visitor.
     *
     * @param executor  the client that is calling the command
     * @param arguments visitorID: unique 10-digit ID of the visitor
     *                  id: comma-separated list of IDs for the books to be
     *                  borrowed by the visitor
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... arguments) {
        //Checking that the amount of arguments is correct
        if (arguments.length < 2) {
            executor.sendMessage("Incorrect Number of Arguments.");
            return ResponseFlag.SUCCESS;
        }

        //Checking that the id passed was a number
        long visitorID;
        try {
            visitorID = parseLong(arguments[0]);
        } catch (NumberFormatException n) {
            executor.sendMessage("Visitor ID must be a 10-digit number.");
            return ResponseFlag.SUCCESS;
        }

        //TODO: Check that the visitor will have less than 5 borrowed books

        //TODO: Check that the visitor has no fines

        //Checking that the book ids are all numbers
        String[] ids = arguments[1].split(",");
        long[] bookIDs = new long[ids.length];
        for (int i = 0; i < ids.length; i++){
            try{
                bookIDs[i] = parseLong(ids[i]);
            } catch (NumberFormatException n){
                executor.sendMessage("Book ID's must be a 13-digit number");
                return ResponseFlag.SUCCESS;
            }
        }

        //TODO: Search the database for the books in bookIDs, if any of them
        // aren't correct, don't allow them to check any of them out

        //TODO: Assign a return-by date to the book and check out the book to
        // the visitor

        //TODO: Change the database to reflect the checked out book

        return ResponseFlag.NOT_FINISHED;
    }
}
