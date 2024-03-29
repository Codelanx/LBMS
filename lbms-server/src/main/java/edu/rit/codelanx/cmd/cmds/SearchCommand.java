package edu.rit.codelanx.cmd.cmds;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String[] AUTHOR_WILDCARD = new String[] {""};

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
                .argumentOptional("title")
                .listOptional("authors")
                .argumentOptional("isbn")
                .argumentOptional("publisher")
                .argumentOptional("sort-order");
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "search";
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
        return  this.execute(executor, args[0], args[2], args[3], args[4], TextInterpreter.splitInput(args[1]));

    }

    /**
     * Whenever this command is called, it will search books that can be
     * purchased by the library (and added to its collection).
     *
     * @param executor the client that is calling the command
     * @param title: title of the book
     * @param authors: comma-separated list of authors of the book
     * @param isbn: International Standard Book NUmber for the book
     * @param publisher: name of the book's publisher
     * @param sortOrder: way to sort the results of the search
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    public ResponseFlag execute(CommandExecutor executor, String title, String isbn,
                                String publisher, String sortOrder, String... authors) {
        if (Arrays.stream(authors).anyMatch(String::isEmpty)) {
            authors = AUTHOR_WILDCARD;
        }
        Set<Long> filterIDs = null;
        if (authors != AUTHOR_WILDCARD) {
            List<Author> found = findAuthors(authors);
            if (!found.isEmpty()) {
                //no results can possibly be found otherwise
                filterIDs = getFilterIDs(found);
                if (filterIDs.isEmpty()) {
                    executor.sendMessage(this.buildResponse(this.getName(), 0));
                    return ResponseFlag.SUCCESS;
                }
            }
        }
        Set<Long> idFilter = filterIDs;
        //at this point, if filterIDs is null, no authors were searched
        //if filterIDs is empty, no listings were found for the authors
        //otherwise, our books are restricted to the contents of filterIDs
        Query<Book> query = this.server.getBookStore().query(Book.class);
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
        if (idFilter != null) {
            query = query.isAny(Book.Field.ID, idFilter);
        }
        Stream<Book> res = query.results();
        switch (sortOrder.toLowerCase()) { //sort-order
            case "":
            case "title":
                res = res.sorted(Comparator.comparing(Book::getTitle));
                break;
            case "publish-date":
                res = res.sorted(Comparator.comparing(Book::getPublishDate).reversed());
                break;
            default:
                executor.sendMessage(buildResponse(this.getName(),"invalid-sort-order"));
                return ResponseFlag.SUCCESS;
        }

        List<Book> bookList = res.collect(Collectors.toList());
        executor.sendMessage(this.buildResponse(this.getName(), bookList.size()));
        if (bookList.isEmpty()) {
            return ResponseFlag.SUCCESS;
        }
        output(executor, bookList);
        return ResponseFlag.SUCCESS;
    }

    /**
     * findAuthors is a helper method for {@link #onExecute} that finds the authors in the database
     * @param authors the authors to search for as strings
     * @return a list of the {@link Author Authors} found in the search
     */
    protected List<Author> findAuthors(String... authors){
        return this.server.getBookStore().query(Author.class)
                .isAny(Author.Field.NAME, authors)
                .results()
                .collect(Collectors.toList());
    }

    /**
     * getFilterIDs is a helper method for {@link #onExecute} that finds book ids based on the author
     * @param found the authors to search for
     * @return a list of the book ids found in the search
     */
    protected Set<Long> getFilterIDs(List<Author> found){
        return this.server.getBookStore()
                .query(AuthorListing.class)
                .isAny(AuthorListing.Field.AUTHOR, found)
                .results()
                .map(AuthorListing::getBook)
                .map(Book::getID)
                .collect(Collectors.toSet());
    }

    /**
     * output is a helper method for {@link #onExecute} that compiles the output for searchcommand and sends it to the executor
     * @param executor the executor that called the command
     * @param bookList the books to list in the output
     */
    protected void output(CommandExecutor executor, List<Book> bookList){
        bookList.stream()
                .map(book -> {
                    List<String> authorsForBook = book.getAuthors().map(Author::getName).collect(Collectors.toList());
                    String authorOutput = this.buildListResponse(authorsForBook.toString());
                    return this.buildResponse(book.getID(), book.getISBN(), book.getTitle(),
                            authorOutput, DATE_FORMAT.format(book.getPublishDate()));
                })
                .forEach(executor::sendMessage);
    }
}
