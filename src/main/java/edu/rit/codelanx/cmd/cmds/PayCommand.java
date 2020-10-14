package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Transaction;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Visitor;

import java.math.BigDecimal;


/**
 * Pays all or part of an outstanding fine.
 * <p>
 * Request Format: pay,visitor ID,amount
 * visitor ID is the unique 10-digit ID of the visitor.
 * amount is the amount that the visitor is paying towards his or her
 * accumulated fines.
 * @author cb4501 Connor Bonitati
 */
public class PayCommand extends TextCommand {

    /**
     * Constructor for the PayCommand class
     *
     * @param server the server that the command is to be run on
     */
    public PayCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("visitor-id")
                .argument("amount");
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "pay";
    }

    /**
     * Whenever this command is called, it will pay the amount towards the
     * specific visitor's negative balance.
     *
     * @param executor  the client that is calling the command
     * @param args      visitorID: unique 10-digit ID of the visitor
     *                  amount: the amount that the visitor is paying toward
     *                      their fines
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {


        //args
        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(args[1]));
        Long visitorID = InputOutput.parseLong(args[0]).orElse(null);


        //finds the visitor
        Visitor visitor = this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, visitorID)
                .results()
                .findAny()
                .orElse(null);

        //checks for arg
        if (visitorID == null) {
            return ResponseFlag.FAILURE;
        }

        //checks to see is the visitor is valid
        else if (visitor == null) {
            executor.sendMessage(buildResponse(getName(), "invalid-visitor-id"));
            return ResponseFlag.SUCCESS;
        }

        //checks that the amount enter is not negative and the amount entered is not greater than balance
        else if ( amount.compareTo(BigDecimal.ZERO) < 1 || visitor.getMoney().compareTo(amount) < 1) {
            executor.sendMessage(buildResponse(getName(), "invalid-amount",
                    amount,visitor.getMoney()));
            return ResponseFlag.SUCCESS;
        } else {
            //performs the transaction
            Transaction.perform(visitor, amount, Transaction.Reason.PAYING_LATE_FEE);
            executor.sendMessage(buildResponse("Success", visitor.getMoney()));
            return ResponseFlag.SUCCESS;
        }

    }

}
