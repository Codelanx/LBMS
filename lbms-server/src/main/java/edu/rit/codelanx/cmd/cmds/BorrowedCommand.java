package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Visitor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Queries for a list of books currently borrowed by a specific visitor.
 * <p>
 * Request Format: borrowed,visitor ID
 * visitor ID is the unique 10-digit ID of the visitor.
 *
 * @author maa1675  Mark Anderson
 */
public class BorrowedCommand extends TextCommand {

    /**
     * Constructor for the BorrowedCommand class
     *
     * @param server the server that the command is to be run on
     */
    public BorrowedCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create().argument("visitor-id");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "borrowed";
    }

    /**
     * {@inheritDoc}
     * @param executor  {@inheritDoc}
     * @param args      {@inheritDoc}
     *                  args[0]: visitorID
     * @return {@inheritDoc}
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        //Checking that the amount of arguments is correct
        if (args.length < 1) {
            executor.sendMessage(buildResponse(this.getName(), "missing" +
                    "-parameters", "visitorID"));
            return ResponseFlag.SUCCESS;
        }

        //Getting the visitor from the id
        Long id = InputOutput.parseLong(args[0]).orElse(null);
        if (id == null) {
            return ResponseFlag.FAILURE;
        }
        //pre: we have a valid id, we need a Visitor
        return this.execute(executor, id);
    }

    /**
     * Whenever this command is called, it will query the database for books
     * being currently borrowed by a visitor.
     *
     * @param executor the client that is calling the command
     * @param visitorID: the id of the visitor to check
     * @return a {@link ResponseFlag} that says whether or not the command was
     * executed correctly
     */
    public ResponseFlag execute(CommandExecutor executor, long visitorID) {

        //query for visitor
        Visitor visitor = getVisitor(visitorID);
        if (visitor == null) {
            executor.sendMessage(buildResponse(this.getName(), "invalid-visitor-id"));
            return ResponseFlag.SUCCESS;
        }

        String responseString = this.getName();

        //if the visitor exist, query for list of books that borrowed by that visitor
        List<Checkout> books = getBorrowedBooks(visitor);

        if (books.size() == 0) {
            executor.sendMessage(buildResponse(responseString, "0"));
            return ResponseFlag.SUCCESS;
        } else {
            responseString += "," + books.size() + "\n";
            getBookResponse(books, responseString);

        }
        executor.sendMessage(responseString);

        return ResponseFlag.SUCCESS;
    }

    /**
     * getVisitor is a helper method for {@link #onExecute}  that gets a visitor from our database
     * @param visitorID the {@link Visitor} to get from the database
     * @return the {@link Visitor} that was found, or null if none found
     */
    protected Visitor getVisitor(Long visitorID) {
        return this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, visitorID)
                .results().findAny().orElse(null);
    }

    /**
     * getBorrowedBooks is a helper method for {@link #onExecute} that gets a list of checkouts for a visitor
     * @param visitor the {@link Visitor} to get the books for
     * @return the {@link Checkout Checkouts} that was found
     */
    protected List<Checkout> getBorrowedBooks(Visitor visitor) {
        return server.getLibraryData().query(Checkout.class)
                .isEqual(Checkout.Field.VISITOR, visitor)
                .isEqual(Checkout.Field.RETURNED, false)
                .results()
                .collect(Collectors.toList());
    }

    /**
     * getBookResponse is a helper method for {@link #onExecute} that adds checkout information to a response string
     * @param books the {@link Checkout Checkouts} to print out
     * @param responseString the current response being built upon
     * @return the modified response string
     */
    protected String getBookResponse(List<Checkout> books, String responseString) {
        for (Checkout c : books) {
            Book b = c.getBook();
            responseString += (buildResponse(b.getID(), b.getISBN(), b.getTitle(), DATE_FORMAT.format(c.getBorrowedAt())) + "\n");
        }
        return responseString;
    }
}
