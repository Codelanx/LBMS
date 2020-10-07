package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.*;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.Optional;

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
     * @param executor  the client that is calling the command
     * @param args title: title of the book
     *                  authors: comma-separated list of authors of the book
     *                  isbn: International Standard Book NUmber for the book
     *                  publisher: name of the book's publisher
     *                  sortorder: way to sort the results of the search
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... args) {

        //Checking that the amount of arguments is correct
        if (CommandUtils.numArgs(args, 1) == UtilsFlag.MISSINGPARAMS) {
            return ResponseFlag.FAILURE;
        }
        int numOfArgs = args.length;
        String title, publisher, sortOrder, isbn = "";
        String[] authors = {};
        if (numOfArgs == 1) {
            title = args[0];
            Optional<? extends Book> bookSearch =
                    this.server.getDataStorage().ofLoaded(Book.class).filter(b -> b.getTitle().equals(title)).findAny();
        }
        //Going through the args and assigning them to their variables
        for (int i = 1; i < numOfArgs; i++) {
            if (i == 1){
                authors = args[i].split(",");
            } else if (i == 2){
                isbn = args[i];
            } else if (i == 3){
                publisher = args[i];
            } else if (i == 4){
                sortOrder = args[i];
            }
        }
        this.server.getDataStorage().query(Book.class)
                .isEqual(Book.Field.TITLE, "hohohoho")
                .results();

        //TODO: Search for the books in the database using the given args
        /*List<Book> bookList = SpecialCommandMethods.getBooks(title, isbn,
                publisher, sortOrder, authors);*/

        //TODO: Create a string of results and send it to the executor
        /*StringBuilder result =
                new StringBuilder(this.getName() + "," + bookList.size() + ",\n");
        for (Book b : bookList){
            result.append(b.getID()).append(",").append(b.getISBN()).append(
                    ",").append(b.getTitle()).append(",").append(b.getAuthors()).append(",").append(b.getPublishDate()).append("\n");
        }
        executor.sendMessage(result);
        */

        return ResponseFlag.NOT_FINISHED;
    }
}
