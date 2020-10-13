package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Searches for books owned by the library and available for borrowing by
 * visitors.
 * <p>
 * Request Format: info,title,{authors},[isbn, [publisher,[sort order]]]
 * title is the title of the book.
 * authors is the comma-separated list of authors of the book.
 * isbn is the 13-digit International Standard Book NUmber (ISBN) for the book.
 * publisher is the name of the book's publisher.
 * sort order is one of: title, publish-date, book-status Sorting of the
 * title will be alphanumerical from 0..1-A-Z, publish date will be newest
 * first, and book status will only show books with at least one copy
 * available for check out.
 */
public class InfoCommand extends TextCommand {

    /**
     * Constructor for the InfoCommand class
     *
     * @param server the server that the command is to be run on
     */
    public InfoCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("title")
                .list("authors", 1)
                .argumentOptional("isbn")
                .argumentOptional("publisher")
                .argumentOptional("sort order");
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "info";
    }

    /**
     * Whenever this command is called, it will search the database for books
     * that are available to be borrowed and owned by the library.
     *
     * @param executor  the client that is calling the command
     * @param args      title: title of the book to be searched
     *                  authors: the comma-separated list of authors of the book
     *                  isbn: the ISBN number for the book
     *                  publisher: the publisher of the book
     *                  sort order: way to sort the result of searching the
     *                              database
     * @return a responseflag that says whether or not the command was
     * executed correctly
     * @author cb4501 Connor Bonitati
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        // gets the number of books
        long numOfBooks = this.server.getLibraryData().query(Book.class)
                .results()
                .count();
        Book bookInfo = this.server.getLibraryData().query(Book.class)
                .results()
                .findAny()
                .orElse(null);


        // gets the number of copies for each book
        long numOfCopies = this.server.getLibraryData().query(Book.class)
                .results()
                .count();
        List<Author> authors = this.server.getLibraryData().query(Author.class)
                .results()
                .collect(Collectors.toList());

        //TODO sorting implementation

        List<Book> result = null; //TODO: end result of our query

        if (args.length == 4){
            switch (args[4].toLowerCase()) {
                case "publish-date":
                    result.sort(Comparator.comparing(Book::getPublishDate));
                    break;
                case "book-status":
                    result.sort(Comparator.comparing(Book::getAvailableCopies));
                    break;
                default:
                    executor.sendMessage(getName() + ",invalid-sort-order");
                    //no break, falls through
                case "title":
                    result.sort(Comparator.comparing(Book::getTitle));
                    break;
            }

        }
        executor.sendMessage(numOfBooks
                + "\n" + numOfCopies + bookInfo.getID() + bookInfo.getPublisher() + args[4]);

        return ResponseFlag.SUCCESS;
    }
}
