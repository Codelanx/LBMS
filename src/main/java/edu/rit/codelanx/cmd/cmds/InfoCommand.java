package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.LBMS;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.loader.Query;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.AuthorListing;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    private static final String[] AUTHOR_WILDCARD = new String[] {""};

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
                .argumentOptional("title")
                .listOptional("authors")
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
        String[] authors = TextInterpreter.splitInput(args[1]);
        if (Arrays.stream(authors).anyMatch(String::isEmpty)) {
            authors = AUTHOR_WILDCARD;
        }
        Set<Long> filterIDs = null;
        if (authors != AUTHOR_WILDCARD) {
            List<Author> found = this.server.getLibraryData().query(Author.class)
                    .isAny(Author.Field.NAME, authors)
                    .results()
                    .collect(Collectors.toList());
            if (!found.isEmpty()) {
                //no results can possibly be found otherwise
                filterIDs = this.server.getLibraryData()
                        .query(AuthorListing.class)
                        .isAny(AuthorListing.Field.AUTHOR, found)
                        .results()
                        .map(AuthorListing::getBook)
                        .map(Book::getID)
                        .collect(Collectors.toSet());
            }
        }
        Set<Long> idFilter = filterIDs;
        //at this point, if filterIDs is null, no authors were searched
        //if filterIDs is empty, no listings were found for the authors
        //otherwise, our books are restricted to the contents of filterIDs
        Query<Book> query = this.server.getLibraryData().query(Book.class);
        //REFACTOR: DRY these blocks of code
        //Going through the query fields and adding them if they are there
        if (!args[0].isEmpty()) {
            query = query.isEqual(Book.Field.TITLE, args[0]);
        }
        if (!args[2].isEmpty()) {
            query = query.isEqual(Book.Field.ISBN, args[2]);
        }
        if (!args[3].isEmpty()) {
            query = query.isEqual(Book.Field.PUBLISHER, args[3]);
        }
        Stream<Book> res;
        if (filterIDs != null) { //if filtering by authors...
            res = filterIDs.isEmpty()
                    ? Stream.empty() //no possible results now
                    : query.results().filter(b -> !idFilter.contains(b.getID()));
        } else {
            res = query.results();
        }
        switch (args[4].toLowerCase()) { //sort-order
            case "":
            case "title":
                res = res.sorted(Comparator.comparing(Book::getTitle));
                break;
            case "publish-date":
                res = res.sorted(Comparator.comparing(Book::getPublishDate).reversed());
                break;
            case "book-status":
                res = res.filter(book -> book.getAvailableCopies() > 0)
                        .sorted(Comparator.comparing(Book::getAvailableCopies));
                break;
            default:
                executor.sendMessage(buildResponse(this.getName(),"invalid" +
                        "-sort-order"));
                return ResponseFlag.SUCCESS;
        }

        List<Book> bookList = res.collect(Collectors.toList());
        executor.sendMessage(this.buildResponse(this.getName(), bookList.size()));
        if (bookList.isEmpty()) {
            return ResponseFlag.SUCCESS;
        }
        bookList.stream()
                .map(book -> {
                    List<String> authorsForBook =
                            book.getAuthors().map(Author::getName).collect(Collectors.toList());
                    String authorOutput =
                            this.buildListResponse(authorsForBook.toString());
                    return this.buildResponse(book.getAvailableCopies(), book.getISBN(), book.getTitle(),
                            authorOutput, book.getPublisher(), DATE_FORMAT.format(book.getPublishDate()),
                            book.getPageCount() + TextCommand.TOKEN_DELIMITER + (LBMS.PREPRODUCTION_DEBUG ? "ID: " + book.getID() : ""));
                })
                .forEach(executor::sendMessage);
        return ResponseFlag.SUCCESS;
    }
}
