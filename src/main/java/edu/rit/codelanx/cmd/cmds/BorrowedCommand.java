package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Visitor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Queries for a list of books currently borrowed by a specific visitor.
 * <p>
 * Request Format: borrowed,visitor ID
 * visitor ID is the unique 10-digit ID of the visitor.
 * @author maa1675  Mark Anderson
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

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create().argument("visitor-id");
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
            executor.sendMessage(buildResponse(this.getName(), "missing" +
                    "-parameters", "visitorID"));
            return ResponseFlag.SUCCESS;
        }

        //Getting the visitor from the id
        Long id = InputOutput.parseLong(args[0]).orElse(null);
        if (id == null) {
            return ResponseFlag.FAILURE;
        }
        //pre: we have a valid id, we need a Visitor
        Visitor visitor = this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, id)
                .results().findAny().orElse(null);
        if (visitor == null) {
            executor.sendMessage(buildResponse(this.getName(), "invalid-id"));
            return ResponseFlag.SUCCESS;
        }

        String responseString = this.getName();

        List<Checkout> books = server.getLibraryData().query(Checkout.class)
                .isEqual(Checkout.Field.VISITOR, visitor)
                .results()
                .collect(Collectors.toList());

        if (books.size() == 0){
            executor.sendMessage(buildResponse(responseString,"0"));
            return ResponseFlag.SUCCESS;
        } else {
            responseString += books.size() + "\n";
            for (Checkout b : books) {
                responseString += (b.getBook().toFormattedText() + "\n");
            }
        }
        executor.sendMessage(buildResponse(responseString));

        return ResponseFlag.SUCCESS;
    }
}
