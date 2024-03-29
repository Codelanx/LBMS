package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Transaction;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
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
 *
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
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "pay";
    }

    /**
     * Whenever this command is called, it will pay the amount towards the
     * specific visitor's negative balance.
     *
     * @param executor the client that is calling the command
     * @param visitorID: unique 10-digit ID of the visitor
     * @param amount: the amount that the visitor is paying toward their fines
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    public ResponseFlag execute(CommandExecutor executor, long visitorID, BigDecimal amount) {

        //finds the visitor
        Visitor visitor= getVisitor(visitorID);
        //checks to see is the visitor is valid
        if (visitor == null) {
            executor.sendMessage(buildResponse(getName(), "invalid-visitor-id"));
            return ResponseFlag.SUCCESS;
        }

        //checks that the amount enter is not negative and the amount entered is not greater than balance
        if (amount.compareTo(BigDecimal.ZERO) < 0 || visitor.getMoney().compareTo(amount.negate()) > 0) {
            executor.sendMessage(buildResponse(getName(), "invalid-amount",
                    amount, visitor.getMoney()));
            return ResponseFlag.SUCCESS;
        }
        //performs the transaction
        String message =performPayTransaction(visitor,amount);
        executor.sendMessage(message);
        return ResponseFlag.SUCCESS;
    }

    /**
     * getVisitor is a helper method for {@link #onExecute}  that gets a visitor from our database
     * @param visitorID the {@link Visitor} to get from the database
     * @return the {@link Visitor} that was found, or null if none found
     */
    protected Visitor getVisitor(long visitorID){
        return this.server.getLibraryData()
                .query(Visitor.class)
                .isEqual(Visitor.Field.ID, visitorID)
                .results()
                .findAny()
                .orElse(null);
    }

    /**
     * performPayTransaction is a helper method for {@link #onExecute} that executes the perform method of Transaction
     * @param visitor the {@link Visitor} to pay the account towards
     * @param amount the amount to pay towards the visitor's account
     * @return a string representing the output for paycommand
     */
    protected String performPayTransaction(Visitor visitor, BigDecimal amount) {
        Transaction.perform(visitor, amount, Transaction.Reason.PAYING_LATE_FEE);
        return buildResponse("Success", visitor.getMoney());

    }

    /**
     * {@inheritDoc}
     * @param executor  {@inheritDoc}
     * @param args      {@inheritDoc}
     *                  args[0]: visitorID
     * @return {@inheritDoc}
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        //args
        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(args[1]));
        Long visitorID = InputOutput.parseLong(args[0]).orElse(null);

        //checks for arg
        if (visitorID == null) {
            executor.sendMessage(buildResponse(getName(), "invalid-visitor-id"));
            return ResponseFlag.FAILURE;
        }
        return this.execute(executor, visitorID, amount);
    }
}
