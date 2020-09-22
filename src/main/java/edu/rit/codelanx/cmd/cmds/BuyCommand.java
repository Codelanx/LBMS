package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.ui.Client;

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
 */
public class BuyCommand extends TextCommand {

    /**
     * Constructor for the BuyCommand class
     *
     * @param server the server that the command is to be run on
     */
    public BuyCommand(Server server) {
        super(server);
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
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
     * @param executor  the client that is calling the command
     * @param arguments buy: name of the command to be run
     *                  quantity: number of copies of each book to purchase
     *                  id: ID of the book as returned by the most recent
     *                  book store search
     *                  ids: comma-separated list of additional books to buy.
     *                  The same quantity of each book will be purchased.
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(Client executor, String... arguments) {
        return ResponseFlag.NOT_FINISHED;
    }
}
