package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.LBMS;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.storage.Query;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.AuthorListing;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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

    private static final String[] AUTHOR_WILDCARD = new String[]{""};

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
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "info";
    }

    /**
     * {@inheritDoc}
     * @param executor  {@inheritDoc}
     * @param args      {@inheritDoc}
     *                  args[0]: title
     *                  args[1]: authors
     *                  args[2]: isbn
     *                  args[3]: publisher
     *                  args[4]: sortOrder
     * @return {@inheritDoc}
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        return this.execute(executor, args[0], args[2], args[3], args[4], TextInterpreter.splitInput(args[1]));
    }

    /**
     * Whenever this command is called, it will search the database for books
     * that are available to be borrowed and owned by the library.
     *
     * @param executor the client that is calling the command
     * @param title: title of the book to be searched
     * @param authors: the comma-separated list of authors of the book
     * @param isbn: the ISBN number for the book
     * @param publisher: the publisher of the book
     * @param sortOrder: way to sort the result of searching the database
     * @return a responseflag that says whether or not the command was
     * executed correctly
     * @author cb4501 Connor Bonitati
     */
    public ResponseFlag execute(CommandExecutor executor, String title, String isbn,
                                String publisher, String sortOrder, String... authors) {
        //if the String author is empty=> string list is empty " "
        if (Arrays.stream(authors).anyMatch(String::isEmpty)) {
            authors = AUTHOR_WILDCARD;
        }

        Set<Long> filterIDs = null;  //list of filter ids
        //if author list is not empty
        if (authors != AUTHOR_WILDCARD) {

            //query list of author in the database
            List<Author> found = findAuthors(authors);

            //if list of author does exists in the database
            if (!found.isEmpty()) {
                //no results can possibly be found otherwise
                filterIDs = getIDs(found);
            }
        }

        Set<Long> idFilter = filterIDs;
        Stream<Book> res = getBookStream(title,isbn,publisher,filterIDs,idFilter);

        switch (sortOrder.toLowerCase()) { //sort-order
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
                executor.sendMessage(buildResponse(this.getName(), "invalid-sort-order"));
                return ResponseFlag.SUCCESS;
        }

        List<Book> bookList = res.collect(Collectors.toList());
        executor.sendMessage(this.buildResponse(this.getName(), bookList.size()));
        if (bookList.isEmpty()) {
            return ResponseFlag.SUCCESS;
        }

        outputInfo(bookList).forEach(executor::sendMessage);
        return ResponseFlag.SUCCESS;
    }


    /**
     * findAuthors is a helper method for {@link #onExecute} that finds the authors in the database
     * @param authors the authors to search for as strings
     * @return a list of the {@link Author Authors} found in the search
     */
    protected List<Author> findAuthors(String... authors) {
        return this.server.getLibraryData().query(Author.class)
                .isAny(Author.Field.NAME, authors)
                .results()
                .collect(Collectors.toList());
    }

    /**
     * getIDs is a helper method for {@link #onExecute} that finds book ids based on the author
     * @param found the authors to search for
     * @return a list of the book ids found in the search
     */
    protected Set<Long> getIDs(List<Author> found) {
        return this.server.getLibraryData()
                .query(AuthorListing.class)
                .isAny(AuthorListing.Field.AUTHOR, found)
                .results()
                .map(AuthorListing::getBook)
                .map(Book::getID)
                .collect(Collectors.toSet());
    }

    /**
     * bookQuery is a helper method for {@link #onExecute} that creates a new query for books on the database
     * @return the resultant {@link Query} created
     */
    protected Query<Book> bookQuery() {
        return this.server
                .getLibraryData()
                .query(Book.class);
    }

    /**
     * bookQuery is a helper method for {@link #onExecute} that creates a stream of mapped books to authors
     * @param bookList the list of {@link Book Books} to map
     * @return a Stream of strings holding the mappings
     */
    protected Stream<String> outputInfo(List<Book> bookList) {
        return bookList.stream().map(book -> {
                    List<String> authorsForBook = book.getAuthors().map(Author::getName).collect(Collectors.toList());
                    String authorOutput = this.buildListResponse(authorsForBook.toString());
                    return this.buildResponse(book.getAvailableCopies(), book.getISBN(), book.getTitle(),
                            authorOutput, book.getPublisher(), DATE_FORMAT.format(book.getPublishDate()),
                            book.getPageCount() + TextCommand.TOKEN_DELIMITER + (LBMS.PREPRODUCTION_DEBUG ? "ID: " + book.getID() : ""));
        });
    }

    /**
     * findBookByAuthor finds a {@link Book} in the database based off of its {@link Author}
     * @param filterIDs the book ids to filter by
     * @param query the query currently in progress
     * @param idFilter the author ids to filter by
     * @return the stream of {@link Book Books} found by the search
     */
    protected Stream<Book> findBookByAuthor(Set<Long> filterIDs, Query<Book> query, Set<Long> idFilter) {
        return filterIDs.isEmpty()
                ? Stream.empty() //no possible results now
                : query.results()
                .filter(b -> !idFilter.contains(b.getID()));
    }

    /**
     * getBookStream is a helper method for {@link #onExecute} that uses the parameters to filter down the book selection
     * @param title the title input by the user to search for
     * @param isbn the isbn input by the user to search for
     * @param publisher the publisher input by the user to search for
     * @param filterIDs the author id to filter by
     * @param idFilter the book id to filter by
     * @return a stream of the {@link Book Books} filtered
     */
    protected Stream<Book> getBookStream(String title, String isbn, String publisher, Set<Long> filterIDs, Set<Long> idFilter){
        Query<Book> query = bookQuery();
        //REFACTOR: DRY these blocks of code
        //Going through the query fields and adding them if they are there
        if (!title.isEmpty()) {
            query = query.isEqual(Book.Field.TITLE, title);
        }
        if (!isbn.isEmpty()) {
            query = query.isEqual(Book.Field.ISBN, isbn);
        }
        if (!publisher.isEmpty()) {
            query = query.isEqual(Book.Field.PUBLISHER, publisher);
        }
        Stream<Book> res;
        if (filterIDs != null) { //if filtering by authors...
            res = findBookByAuthor(filterIDs, query, idFilter);
        } else {
            res = query.results();
        }
        return res;
    }
}
