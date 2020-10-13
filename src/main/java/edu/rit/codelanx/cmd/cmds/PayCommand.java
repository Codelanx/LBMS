package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;


/**
 * Pays all or part of an outstanding fine.
 * <p>
 * Request Format: pay,visitor ID,amount
 * visitor ID is the unique 10-digit ID of the visitor.
 * amount is the amount that the visitor is paying towards his or her
 * accumulated fines.
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


        /*//args
        BigDecimal amount = new BigDecimal(Double.parseDouble(args[1]));
        long visitorID = Long.parseLong(args[0]);

        //finds the visitor by id
        Visitor visitor = findVisitor(this.server, visitorID);

        assert visitor == null;
        //checks to make sure the id is valid
        if (!visitor.isValid()) {
            executor.sendMessage(getName() + ", invalid-visitor-id");
            return ResponseFlag.SUCCESS;
        }

        //checks to see if the amount is greater than 0
        if (amount.compareTo(BigDecimal.ZERO) < 1 || visitor.getMoney().compareTo(amount) < 1) {
            executor.sendMessage(getName() + ", invalid-amount" + "," + amount + "," + visitor.getMoney());
            return ResponseFlag.FAILURE;

        } else {
            //Performs the transaction
            Transaction.perform(visitor, amount, Transaction.Reason.PAYING_LATE_FEE);
            executor.sendMessage("Success" + visitor.getMoney());
            return ResponseFlag.SUCCESS;
        }*/
        return ResponseFlag.NOT_FINISHED;
    }

}
