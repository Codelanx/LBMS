package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.data.state.types.Visit;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Returns a book borrowed by a library visitor
 * <p>
 * Request Format: return,visitor ID, id[,ids]
 * visitor ID is the unique 10-digit ID of the visitor.
 * id is the ID of the book as assigned in the most recent borrowed books query.
 * ids is a comma-separated list of the IDs of additional books to return.
 * @author sja9291  Spencer Alderman
 * @see TextCommand - for document link to LBMS command specification
 */
public class ReturnCommand extends TextCommand {

    /**
     * Constructor for the RegisterCommand class
     * @param server the server that the command is to be run on
     */
    public ReturnCommand(Server<TextMessage> server) {
        super(server);
    }

    /**
     * {@inheritDoc}
     *
     * Command format (minus {@link #getName}): visitor-id, id[, id...]
     * where id == book ID (requested output is requested output I suppose)
     *
     * @return {@inheritDoc}
     */
    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("visitor-id")
                .list("id", 1);
    }


    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "return";
    }




    /**
     *  Whenever this command is called, it will return a borrowed book back to the library
     *  using a visitors id
     *
     * @param executor the client that is calling the command
     * @param args      visitor ID: the unique 10-digit ID of the visitor
     *                  id: the ID of the book borrowed
     *                  ids: the comma-separated list of the IDs od additional books to return
     *                  args[0] - Visitor ID
     *                  args[1+] - Book IDs to be returned
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        Optional<Long> optID = InputOutput.parseLong(args[0]);


        if (!optID.isPresent()) {
            executor.sendMessage(this.buildResponse(this.getName(), "invalid-visitor-id"));
            return ResponseFlag.SUCCESS;
        }

        Set<Long> ids = new LinkedHashSet<>();
        for (int i = 1; i < args.length; i++) {
            Optional<Long> parsed = InputOutput.parseLong(args[1]);
            parsed.ifPresent(ids::add);
            if (!parsed.isPresent()) {
                executor.sendMessage("invalid-bookID");
                return ResponseFlag.SUCCESS;
            }
        }
        return this.execute(executor, optID.get(), ids.stream().mapToLong(l -> l).toArray());
    }

    public ResponseFlag execute(CommandExecutor executor, long visitorID, long... bookIDs) {

        Visitor visitor = queryVisitor(visitorID);

        if (visitor == null) {
            executor.sendMessage("invalid-visitorID");
            return ResponseFlag.SUCCESS;
        }

        //parse the remainder of the arguments into book ids
        List<String> failed = new LinkedList<>();

        //now, make sure the ids that we parsed are valid books
        Set<Long> ids = LongStream.of(bookIDs).boxed().collect(Collectors.toSet());
        Set<Book> books = queryBooks(bookIDs);

        if (books.size() != bookIDs.length) {
            //don't have time for fast disjoint sets, so...
            books.stream().map(Book::getID).forEach(ids::remove);
            //ids is now a set of invalid ids
            ids.stream().map(Object::toString).forEach(failed::add);
        }

        List<Checkout> checkouts = null;
        if (failed.isEmpty()) {
            //Make sure the books are actually checked out
            checkouts = queryCheckouts(visitor, books);
            if (checkouts.size() != books.size()) {
                //again, invalid books because they weren't checks out
                checkouts.stream().map(Checkout::getBook).forEach(books::remove);
                //books is now the invalid copies
                books.stream().map(Book::getID)
                        .map(Object::toString)
                        .forEach(failed::add);
            }
        }
        if (!failed.isEmpty()) {
            failed.add(0, this.getName());
            failed.add(1, "invalid-book-id");
            executor.sendMessage(this.buildResponse(failed));
            return ResponseFlag.SUCCESS;
        }
        //only successful checkouts found at this point
        List<String> fined = new LinkedList<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (Checkout checkout : checkouts) {
            BigDecimal fine = checkout.returnBook(this.server.getClock());
            if (fine != null) {
                fined.add(checkout.getBook().getID() + "");
                sum = sum.add(fine);
            }
        }
        if (fined.isEmpty()) {
            executor.sendMessage(this.buildResponse(this.getName(), "success"));
            return ResponseFlag.SUCCESS;
        }
        fined.add(0, this.getName());
        fined.add(1, "overdue");
        fined.add(2, String.format("$%.2f", sum.doubleValue()));
        executor.sendMessage(this.buildResponse(fined));
        return ResponseFlag.SUCCESS;
    }

    protected Visitor queryVisitor(long visitorID){
        return this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, visitorID)
                .results().findAny().orElse(null);
    }

    protected Set<Book> queryBooks(long... bookIDs){
        return this.server.getLibraryData().query(Book.class)
                .isAny(Book.Field.ID, LongStream.of(bookIDs).boxed().toArray(Long[]::new))
                .results()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    protected List<Checkout> queryCheckouts(Visitor visitor, Set<Book> books){
        return this.server.getLibraryData().query(Checkout.class)
                .isEqual(Checkout.Field.VISITOR, visitor)
                .isAny(Checkout.Field.BOOK, books)
                .isEqual(Checkout.Field.RETURNED, false)
                .results()
                .collect(Collectors.toList());
    }
}