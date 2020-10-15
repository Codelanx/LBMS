package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.loader.ProxiedStateBuilder;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.AuthorListing;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

/**
 * Purchases one or more books returned from the last book store search.
 * Purchased books are added to the library's collection. If the books
 * already exist in the collection, the available quantity of each book is
 * updated to reflect the newly purchased books.
 * <p>
 * Request Format: buy,quantity,id[,ids]
 * quantity is the number of copies of each book to purchase.
 * id is the ID of the book as returned by the most recent book store search.
 * ids is the comma-separated list of additional books to buy. The same
 * quantity of each book will be purchased.
 *
 * @author maa1675  Mark Anderson
 */
public class BuyCommand extends TextCommand {

    /**
     * Constructor for the BuyCommand class
     *
     * @param server the server that the command is to be run on
     */
    public BuyCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("quantity")
                .list("id", 1);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "buy";
    }

    /**
     * Whenever this command is called, it will purchase some of the books
     * returned from the last search and add them to the library's database
     * of books
     *
     * @param executor the client that is calling the command
     * @param args     quantity: number of copies of each book to purchase
     *                 id(s): 1 or more book IDs to be purchased (separated
     *                 by commas).
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        //Checking that the amount of args passed is correct
        Optional<Integer> quantity = InputOutput.parseInt(args[0]);
        if (!quantity.isPresent() || quantity.get() <= 0) {
            executor.sendMessage(this.buildResponse(this.getName(), "invalid-quantity", args[0]));
            return ResponseFlag.SUCCESS;
        }
        //TODO: DRY, ran out of time
        //parse the remainder of the arguments into book ids
        List<String> failed = new LinkedList<>();
        Set<Long> ids = new LinkedHashSet<>();
        for (int i = 1; i < args.length; i++) {
            Optional<Long> parsed = InputOutput.parseLong(args[i]);
            parsed.ifPresent(ids::add);
            if (!parsed.isPresent()) {
                failed.add(args[i]);
            }
        }
        Set<Book> books = null;
        if (failed.isEmpty()) {
            //now, make sure the ids that we parsed are valid books
            books = this.server.getBookStore().query(Book.class)
                    .isAny(Book.Field.ID, ids)
                    .results()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (books.size() != ids.size()) {
                //don't have time for fast disjoint sets, so...
                books.stream().map(Book::getID).forEach(ids::remove);
                //ids is now a set of invalid ids
                ids.stream().map(Object::toString).forEach(failed::add);
            }
        }
        if (!failed.isEmpty()) {
            failed.add(0, this.getName());
            failed.add(1, "invalid-book-id");
            executor.sendMessage(this.buildResponse(failed));
            return ResponseFlag.SUCCESS;
        }
        //okay, we're all set with valid inputs
        executor.sendMessage(this.buildResponse(this.getName(), "success", books.size()));
        this.server.getLibraryData().query(Book.class)
                .isAny(Book.Field.ISBN, books.stream().map(Book::getISBN).collect(Collectors.toSet()))
                .results()
                .peek(b -> b.addCopy(1))
                .peek(books::remove) //handled local books first
                .map(b -> this.bookToString(b, quantity.get()))
                .forEach(executor::sendMessage);
        //otherwise
        Map<Book, Set<Author>> authors = books.stream()
                .collect(Collectors.toMap(Function.identity(),
                        b -> b.getAuthors().map(AuthorListing::getAuthor).collect(Collectors.toSet())));
        Set<String> names = authors.values().stream().flatMap(Set::stream).map(Author::getName).collect(Collectors.toSet());
        Map<String, Author> known = this.server.getLibraryData().query(Author.class)
                .isAny(Author.Field.NAME, names)
                .results().collect(Collectors.toMap(Author::getName, Function.identity()));
        known.keySet().forEach(names::remove);
        //names is now unknown authors
        names.stream()
                .map(s -> Author.create().setValue(Author.Field.NAME, s).build(this.server.getLibraryData()))
                .forEach(a -> known.put(a.getName(), a));
        //known contains all relevant author objects, and thus so does our local states
        // (they haven't fallen out of ref yet)
        authors.forEach((book, auths) -> {
            Set<Author> relevant = auths.stream().map(Author::getName).map(known::get).collect(Collectors.toSet());
            StateBuilder<Book> sb = new ProxiedStateBuilder<>(book);
            sb.setValue(Book.Field.TOTAL_COPIES, quantity.get());
            sb.setValue(Book.Field.CHECKED_OUT, 0);
            Book created = this.server.getLibraryData().insert(sb);
            relevant.forEach(auth -> {
                AuthorListing.create()
                        .setValue(AuthorListing.Field.BOOK, created)
                        .setValue(AuthorListing.Field.AUTHOR, auth)
                        .build(this.server.getLibraryData());
            });
            executor.sendMessage(this.bookToString(created, quantity.get()));
        });
        return ResponseFlag.SUCCESS;
    }

    private String bookToString(Book book, int quantity) {
        List<String> authors = book.getAuthors().map(AuthorListing::getAuthor).map(Author::getName).collect(Collectors.toList());
        return String.join(TextCommand.TOKEN_DELIMITER, book.getISBN(),
                book.getTitle(),
                this.buildListResponse(authors),
                DATE_FORMAT.format(book.getPublishDate()),
                quantity + "");
    }
}
