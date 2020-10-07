package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandUtils;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Visitor;

/**
 * Queries for a list of books currently borrowed by a specific visitor.
 * <p>
 * Request Format: borrowed,visitor ID
 * visitor ID is the unique 10-digit ID of the visitor.
 */
public class BorrowedCommand extends TextCommand {

    /**
     * Constructor for the BorrowedCommand class
     *
     * @param server the server that the command is to be run on
     */
    public BorrowedCommand(Server<TextMessage> server) {
        super(server);
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "borrowed";
    }

    /**
     * Whenever this command is called, it will query the database for books
     * being currently borrowed by a visitor.
     *
     * @param executor  the client that is calling the command
     * @param args visitorID: the id of the visitor to check
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        //Checking that the amount of arguments is correct
        if (args.length < 1) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID");
            return ResponseFlag.SUCCESS;
        } else if (args.length > 1){
            return ResponseFlag.FAILURE;
        }

        //Making sure that the visitor ID is correctly formatted
        long visitorID = CommandUtils.checkVisitorID(args[0]);
        if (visitorID == -1){
            return ResponseFlag.FAILURE;
        }


        //Finding the visitor with the matching ID in the database
        Visitor v = CommandUtils.findVisitor(this.server, visitorID);
        if (v == null){
            executor.sendMessage(this.getName() + ",invalid-visitor-id;");
            return ResponseFlag.SUCCESS;
        }

        //TODO: How to get the books checked out by a visitor
        /*String responseString = this.getName() + ",";
        if (v.getCheckedOut.size() == 0){
            executor.sendMessage(this.getName() + ",0;");
            return ResponseFlag.SUCCESS;
        } else {
            responseString += v.getCheckedOut.size();
            for (Book b : v.getCheckedOut()){
                Map<String, Object> bookData = b.getData();
                responseString += ("\n" + bookData.get("id") + "," + bookData.get("isbn") +
                        "," + bookData.get("title") + "," + bookData.get(
                                "date_borrowed;");
            }
            executor.sendMessage(responseString);
        }
        return ResponseFlag.SUCCESS;*/

        return ResponseFlag.NOT_FINISHED;
    }
}
