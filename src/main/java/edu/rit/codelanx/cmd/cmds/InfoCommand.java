package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.loader.Query;
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
     * @param executor the client that is calling the command
     * @param args     title: title of the book to be searched
     *                 authors: the comma-separated list of authors of the book
     *                 isbn: the ISBN number for the book
     *                 publisher: the publisher of the book
     *                 sort order: way to sort the result of searching the
     *                 database
     * @return a responseflag that says whether or not the command was
     * executed correctly
     * @author cb4501 Connor Bonitati
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        if (args.length < 2) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "title,authors,isbn,publisher,sortOrder;");
            return ResponseFlag.SUCCESS;
        }

        //Going through the args and assigning them to their variables
        String title = args[0],
                isbn = args[2],
                publisher = args[3],
                sortOrder = args[4];
        String[] authors = args[1].split(TextCommand.TOKEN_DELIMITER);

        Query<Book> query = this.server.getLibraryData().query(Book.class);
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

        List<Book> bookList;

        switch (sortOrder) {
            case "title":
                bookList = query.results().collect(Collectors.toList());
                bookList.sort(Comparator.comparing(Book::getTitle));
                break;
            case "publish-date":
                bookList = query.results().collect(Collectors.toList());
                bookList.sort(Comparator.comparing(Book::getPublishDate).reversed());
                break;
            case "book-status":
                bookList =
                        query.results().filter(Book::isValid).collect(Collectors.toList());
                break;
            default:
                executor.sendMessage(this.getName() + ",invalid-sort-order");
                return ResponseFlag.SUCCESS;
        }

        executor.sendMessage(this.buildResponse(this.getName(),
                bookList.size()));
        if (bookList.isEmpty()) {
            return ResponseFlag.SUCCESS;
        }
        executor.sendMessage("Results (" + bookList.size() + "):");
        bookList.stream()
                .map(book -> {
                    List<String> authorsForBook =
                            book.getAuthors().map(Author::getName).collect(Collectors.toList());
                    String authorOutput =
                            this.buildListResponse(authorsForBook.toString());
                    return this.buildResponse(book.getAvailableCopies(),
                            book.getID(), book.getTitle(),
                            authorOutput, book.getPublisher(),
                            book.getPublishDate(), book.getPageCount());
                })
                .forEach(executor::sendMessage);
        return ResponseFlag.SUCCESS;
    }
}
