package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Visitor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

/**
 * Borrows a book for a visitor; uses the ID of a specific book or books
 * returned in the most recent library book search.
 * <p>
 * Request Format: borrow,visitor ID,{id};
 * visitor ID is the unique 10-digit ID of the visitor.
 * id is the comma-separated list of IDs for the books to be borrowed by the
 * visitor.
 *
 * @author maa1675  Mark Anderson
 */
public class BorrowCommand extends TextCommand {

    //maximum number of books that can be checked out
    private static final int BOOK_LIMIT = 5;

    /**
     * Constructor for the BorrowCommand class
     *
     * @param server the server that the command is to be run on
     */
    public BorrowCommand(Server<TextMessage> server) {
        super(server);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
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
        if (!this.server.getLibraryData().getLibrary().isOpen()){
            executor.sendMessage(buildResponse(this.getName(), "library-is-closed"));
            return ResponseFlag.FAILURE;
        }

        if (args.length < 2) {
            executor.sendMessage(buildResponse(this.getName(), "missing" +
                    "-parameters", "visitorID"));
            return ResponseFlag.SUCCESS;
        }

        long visitorID;
        Set<Long> bookIDs = new HashSet<>();
        //Checking that the id passed was a number
        try {
            visitorID = parseLong(args[0]);
            String[] books = TextInterpreter.splitInput(args[1]);
            for (String b : books){
                bookIDs.add(parseLong(b));
            }
        } catch (NumberFormatException e) {
            return ResponseFlag.SUCCESS;
        }

        Visitor v = getVisitor(visitorID);
        if (v == null) {
            executor.sendMessage(buildResponse(this.getName(), "invalid-visitor-id"));
            return ResponseFlag.SUCCESS;
        }

        //Query for checkout
        long checkedOutBooks = getCheckedOut(v);

        //Checking they don't or won't have too many books
        if (checkedOutBooks >= BOOK_LIMIT || checkedOutBooks + bookIDs.size() > BOOK_LIMIT) {
            executor.sendMessage(buildResponse(this.getName(), "book-limit-exceeded"));
            return ResponseFlag.SUCCESS;
        }

        //Checking that the visitor's account balance is in the positive
        if (v.getMoney().compareTo(BigDecimal.ZERO) < 0) {
            executor.sendMessage(buildResponse(this.getName(), "outstanding-fine", v.getMoney()));
            return ResponseFlag.SUCCESS;
        }

        //Search the database for the books in bookIDs, if any of them
        // aren't correct, don't allow them to check any of them out
        List<Book> foundBooks = getBooks(bookIDs);

        if (foundBooks.size() != bookIDs.size()) {
            //we had a mismatch
            foundBooks.stream().map(Book::getID).forEach(bookIDs::remove);
            //bookIDs now contains only invalid ids
            List<String> out = new LinkedList<>();
            bookIDs.forEach(id -> out.add(id + ""));
            out.add(0, this.getName());
            out.add(1, "invalid-book-id");
            executor.sendMessage(buildResponse(out));
            return ResponseFlag.SUCCESS;
        }

        for (Book book : foundBooks){
            if (book.getAvailableCopies() == 0){
                executor.sendMessage(buildResponse(this.getName(), "no-copies-available", book.getTitle()));
                return ResponseFlag.SUCCESS;
            }
        }

        checkout(foundBooks, v);

        Instant due = getDueDate();
        executor.sendMessage(buildResponse(this.getName(), DATE_FORMAT.format(due)));

        return ResponseFlag.SUCCESS;
    }

    protected Visitor getVisitor(Long id){
        return this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, id)
                .results().findAny().orElse(null);
    }

    protected List<Book> getBooks(Set<Long> ids){
        return this.server.getLibraryData().query(Book.class)
                .isAny(Book.Field.ID, ids)
                .results()
                .collect(Collectors.toList());
    }

    protected long getCheckedOut(Visitor v){
        return server.getLibraryData().query(Checkout.class)
                .isEqual(Checkout.Field.VISITOR, v)
                .results().count();
    }

    protected void checkout(List<Book> books, Visitor v){
        books.forEach(b -> b.checkout(v, this.server.getClock()));
    }

    protected Instant getDueDate(){
        return server.getClock().getCurrentTime().plus(Duration.ofDays(Checkout.BORROW_DAYS));
    }
}
