package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.*;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.loader.Query;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Searches for books that may be purchased by the library and added to its
 * collection.
 * <p>
 * Request Format: search,title,[{authors},isbn[,publisher[,sort order]]]
 * title is the title of the book.
 * authors is the comma-separated list of authors of the book.
 * isbn is the 13-digit International Standard Book NUmber (ISBN) for the book.
 * publisher is the name of the book's publisher.
 * sort order is one of: title, publish-date. Sorting of the title will be
 * alphanumerical from 0..1-A-Z, publish date will be newest first.
 */
public class SearchCommand extends TextCommand {

    /**
     * Constructor for the SearchCommand class
     *
     * @param server the server that the command is to be run on
     */
    public SearchCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("title")
                .listOptional("authors")
                .argumentOptional("isbn")
                .argumentOptional("publisher")
                .argumentOptional("sort-order");
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "search";
    }

    /**
     * Whenever this command is called, it will search books that can be
     * purchased by the library (and added to its collection).
     *
     * @param executor the client that is calling the command
     * @param args     title: title of the book
     *                 authors: comma-separated list of authors of the book
     *                 isbn: International Standard Book NUmber for the book
     *                 publisher: name of the book's publisher
     *                 sortorder: way to sort the results of the search
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... args) {
        //args == {title, authors, isbn, publisher, sort-order}
        //if optional and omitted, value is null
        //if value is a list, value.split(TextCommand.TOKEN_DELIMITER) will
        // provide the subarguments of that argument

        //Checking that the amount of arguments is correct
        if (args.length < 2) {
            return ResponseFlag.FAILURE;
        }

        //Going through the args and assigning them to their variables
        String title = args[0],
                isbn = args[2],
                publisher = args[3],
                sortOrder = args[4];
        String[] authors = TextInterpreter.splitInput(args[1]);

        Query<Book> query = this.server.getBookStore().query(Book.class);
        //Going through the query fields and adding them if they are there
        if (!title.isEmpty() && !title.equals("*")) {
            query = query.isEqual(Book.Field.TITLE, title);
        }
        if (authors.length > 0 && !authors[0].equals("*")) {
            for (String authorString : authors) {
                Author a = this.server.getLibraryData().query(Author.class)
                        .isEqual(Author.Field.NAME, authorString)
                        .results()
                        .findAny()
                        .orElse(null);
                if (a != null) {
                    query = query.isEqual(Book.Field.ID, a.getID());
                } else {
                    return ResponseFlag.FAILURE;
                }
            }
        }
        if (!isbn.isEmpty() && !isbn.equals("*")) {
            query = query.isEqual(Book.Field.ISBN, isbn);
        }
        if (!publisher.isEmpty() && !publisher.equals("*")) {
            query = query.isEqual(Book.Field.PUBLISHER, publisher);
        }

        List<Book> bookList = query.results().collect(Collectors.toList());

        if (sortOrder.equals("") || sortOrder.equals("*") || sortOrder.equals("title")) {
            bookList.sort(Comparator.comparing(Book::getTitle));
        } else {
            bookList.sort(Comparator.comparing(Book::getPublishDate).reversed());
        }

        //this.getName() + "," + 0 + ";"
        executor.sendMessage(this.buildResponse(this.getName(), bookList.size()));
        if (bookList.isEmpty()) {
            return ResponseFlag.SUCCESS;
        }
        executor.sendMessage("Results (" + bookList.size() + "):");
        bookList.stream()
                .map(book -> {
                    List<String> authorsForBook =
                            book.getAuthors().map(Author::getName).collect(Collectors.toList());
                    String authorOutput = this.buildListResponse(authorsForBook.toString());
                    return this.buildResponse(book.getID(), book.getISBN(),
                            book.getTitle(), authorOutput,
                            DATE_FORMAT.format(book.getPublishDate()));
                })
                .forEach(executor::sendMessage);

        return ResponseFlag.SUCCESS;
    }
}
