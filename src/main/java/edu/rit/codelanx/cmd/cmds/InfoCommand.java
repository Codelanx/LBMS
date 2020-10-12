package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static edu.rit.codelanx.cmd.CommandUtils.numArgs;

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
                .argument("authors")
                .argument("isbn")
                .argument("publisher")
                .argument("sort order");
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
     * @param args info: the name of the command to be run
     *                  title: title of the book to be searched
     *                  authors: the comma-separated list of authors of the book
     *                  isbn: the ISBN number for the book
     *                  publisher: the publisher of the book
     *                  sort order: way to sort the result of searching the
     *                  database
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        // gets the number of books
        long numOfBooks = this.server.getDataStorage().query(Book.class)
                .results()
                .count();

        // gets the number of copies for each book
        Book numOfCopies = this.server.getDataStorage().query(Book.class)
                .results()
                .findAny()
                .orElse(null);

        numOfCopies.getTotalCopies();

        List<Author> authors = this.server.getDataStorage().query(Author.class)
                .results()
                .collect(Collectors.toList());

        //TODO sorting implementation

        executor.sendMessage(numOfBooks
                + "\n" + numOfCopies);

        return ResponseFlag.SUCCESS;
    }
}
