package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;

import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Visitor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    @Override
    public TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("visitor-id")
                .list("book-id", 1);
    }

    /**
     * Whenever this command is called, it will borrow a book for a visitor.
     *
     * @param executor the client that is calling the command
     * @param args     {@inheritDoc}
     *                 args[0]: long, unique 10-digit ID for {@link Visitor}
     *                 args[1+]: Books for the visitor to borrow
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        if (args.length < 2) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID;");
            return ResponseFlag.SUCCESS;
        }

        long visitorID;
        Set<Long> bookIDs = new HashSet<Long>();
        //Checking that the id passed was a number
        try {
            visitorID = parseLong(args[0]);
            for (int i = 1; i < args.length; i++) {
                bookIDs.add(parseLong(args[i]));
            }
        } catch (NumberFormatException e) {
            return ResponseFlag.FAILURE;
        }

        //Finding the visitor with the matching ID in the database
        Visitor v =
                this.server.getLibraryData().query(Visitor.class).isEqual(Visitor.Field.ID, visitorID).results().findAny().orElse(null);
        if (v == null) {
            executor.sendMessage(this.getName() + ",invalid-visitor-id;");
            return ResponseFlag.SUCCESS;
        }

        //Query for checkout
        long checkedOutBooks = server.getLibraryData()
                .query(Checkout.class).isEqual(Checkout.Field.VISITOR, v).results().count();

        //Checking they don't or won't have too many books
        if (checkedOutBooks > 5 || checkedOutBooks + bookIDs.size() > 5) {
            executor.sendMessage(this.getName() + ",book-limit-exceeded;");
        }

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
            bookSearch = this.server.getLibraryData().query(Book.class)
                                .isEqual(Book.Field.ID, bookID)
                                .results().findAny();
            if (bookSearch.isPresent()) {
                books.add(bookSearch.get());
            } else {
                executor.sendMessage(this.getName() + ",invalid-book-id," + bookID);
                return ResponseFlag.SUCCESS;
            }
        }

        for (Book b : books) {
            b.checkout(v);
        }

        return ResponseFlag.SUCCESS;
    }
}
