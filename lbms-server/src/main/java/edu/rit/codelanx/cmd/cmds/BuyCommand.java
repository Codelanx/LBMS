package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.storage.ProxiedStateBuilder;
import edu.rit.codelanx.data.storage.StateBuilder;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        //Making sure the quantity is an int
        int quantity;
        try {
            quantity = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return ResponseFlag.FAILURE;
        }

        if (quantity == 0) {
            executor.sendMessage(buildResponse(this.getName(),"success", quantity));
            return ResponseFlag.SUCCESS;
        }

        Set<Long> bookIDs = new HashSet<Long>();
        //Checking that the ids passed was a number
        try {
            for (int i = 1; i < args.length; i++) {
                bookIDs.add(parseLong(args[i]));
            }
        } catch (NumberFormatException e) {
            return ResponseFlag.FAILURE;
        }
        return this.execute(executor, quantity, bookIDs.stream().mapToLong(l -> l).toArray());
    }

    public ResponseFlag execute(CommandExecutor executor, int copies, long... bookIDs) {
        List<Book> bookList = new ArrayList<>();
        for (Long id : bookIDs) {
            Book bookSearch = queryLibrary(id);
            if (bookSearch != null) {
                bookSearch.addCopy(copies);
                bookList.add(bookSearch);
            } else {
                Book newBook = queryBookstore(id);
                if (newBook != null) {
                    newBook = bookstoreToLibrary(newBook, copies);
                    bookList.add(newBook);
                } else {
                    executor.sendMessage(buildResponse(this.getName(),"failure", copies));
                    return ResponseFlag.FAILURE;
                }
            }
        }

        executor.sendMessage(this.buildResponse(this.getName(), bookList.size()));

        if (bookList.isEmpty()) {
            executor.sendMessage(buildResponse(this.getName(),"success", copies));
            return ResponseFlag.SUCCESS;
        }

        executor.sendMessage("Results (" + bookList.size() + "):");
        bookList.stream()
                .map(book -> {
                    List<String> authorsForBook =
                            book.getAuthors().map(Author::getName).collect(Collectors.toList());
                    String authorOutput =
                            this.buildResponse(authorsForBook.toString());
                    authorOutput = "{" + authorOutput + "}";
                    return this.buildResponse(book.getID(), book.getISBN(),
                            book.getTitle(), authorOutput,
                            DATE_FORMAT.format(book.getPublishDate()));
                })
                .forEach(executor::sendMessage);

        executor.sendMessage(buildResponse(this.getName(),"success", copies));
        return ResponseFlag.SUCCESS;

    }

    protected Book queryLibrary(Long id){
        return server.getLibraryData().query(Book.class)
                .isEqual(Book.Field.ID, id)
                .results().findAny().orElse(null);
    }

    protected Book queryBookstore(Long id){
        return server.getBookStore().query(Book.class).isEqual(Book.Field.ID, id).results().findAny().orElse(null);
    }

    protected Book bookstoreToLibrary(Book newBook, int copies){
        StateBuilder<Book> bookStateBuilder = new ProxiedStateBuilder<>(newBook);
        bookStateBuilder.setValue(Book.Field.TOTAL_COPIES, copies);
        bookStateBuilder.setValue(Book.Field.CHECKED_OUT, 0);
        return this.server.getLibraryData().insert(bookStateBuilder);
    }
}