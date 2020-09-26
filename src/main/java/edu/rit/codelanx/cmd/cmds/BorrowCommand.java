package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.SpecialCommandMethods;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.types.Book;
import edu.rit.codelanx.data.types.Visitor;
import edu.rit.codelanx.ui.Client;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static edu.rit.codelanx.cmd.CommandUtils.findVisitor;
import static edu.rit.codelanx.cmd.CommandUtils.numArgs;
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
    public BorrowCommand(Server<TextMessage> server) {
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
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... arguments) {

        if (numArgs(arguments, 2) == UtilsFlag.MISSINGPARAMS) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID;");
            return ResponseFlag.SUCCESS;
        }

        long visitorID;
        Set<Long> bookIDs = new HashSet<Long>();
        //Checking that the id passed was a number
        try {
            visitorID = parseLong(arguments[0]);
            for (int i = 1; i < arguments.length; i++) {
                bookIDs.add(parseLong(arguments[i]));
            }
        } catch (NumberFormatException e) {
            return ResponseFlag.FAILURE;
        }

        //Finding the visitor with the matching ID in the database
        Visitor v = findVisitor(this.server, visitorID);
        if (v == null) {
            executor.sendMessage(this.getName() + ",invalid-visitor-id;");
        }

        //TODO: Check that the visitor will have less than 5 borrowed books
        /*
            if (v.getCheckedOut.size() > 5 || v.getCheckedOut.size() +
            bookIDs.size() > 5){
                executor.sendMessage(this.getName() + ",book-limit-exceeded;");
                return ResponseFlag.Success;
            }
         */

        //Checking that the visitor's account balance is in the positive
        if (v.getMoney().compareTo(BigDecimal.ZERO) < 1) {
            executor.sendMessage(this.getName() + ",outstanding-fine," + v.getMoney());
            return ResponseFlag.SUCCESS;
        }

        //Search the database for the books in bookIDs, if any of them
        // aren't correct, don't allow them to check any of them out
        Set<Book> books = new HashSet<>();
        Optional<? extends Book> bookSearch;
        for (final long bookID : bookIDs) {
            bookSearch =
                    this.server.getDataStorage().ofLoaded(Book.class).filter(b -> b.getID() == bookID).findAny();
            if (bookSearch.isPresent()) {
                books.add(bookSearch.get());
            } else {
                executor.sendMessage(this.getName() + ",invalid-book-id," + bookID);
                return ResponseFlag.SUCCESS;
            }
        }

        //TODO: Assign a return-by date to the books and check out the books to
        // the visitor
        /*for (Book b : books){
            b.checkout(v);
        }
        return ResponseFlag.SUCCESS;
        */

        return ResponseFlag.NOT_FINISHED;
    }
}
