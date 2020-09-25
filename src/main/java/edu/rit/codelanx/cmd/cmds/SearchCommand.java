package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

/**
 * Searches for books that may be purchased by the library and added to its collection.
 *
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
    public SearchCommand(Server server) {
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
     * @param arguments search: name of the command to be run
     *                  title: title of the book
     *                  authors: comma-separated list of authors of the book
     *                  isbn: International Standard Book NUmber for the book
     *                  publisher: name of the book's publisher
     *                  sortorder: way to sort the results of the search
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... arguments) {
        return ResponseFlag.NOT_FINISHED;
    }
}
