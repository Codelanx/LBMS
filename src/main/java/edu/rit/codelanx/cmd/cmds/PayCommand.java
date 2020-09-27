package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.data.types.Visit;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.types.Visitor;

import java.math.BigDecimal;
import java.util.Optional;

import static edu.rit.codelanx.cmd.CommandUtils.findVisitor;
import static edu.rit.codelanx.cmd.CommandUtils.numArgs;

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
        //Checking that they have the correct amount of parameters
        if (numArgs(args, 2) == UtilsFlag.MISSINGPARAMS) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID;");
            return ResponseFlag.SUCCESS;
        }

        //establishes the visitorID in the arguments
        long visitorID = Long.parseLong(args[0]);
        /**
        try {
            visitorID = Long.parseLong(args[0]);
            for (int i = 1; i < args.length; i++) {

            }
        }
         */
        //Finds a visitor
        Visitor visitor = findVisitor(this.server, visitorID);
        //checks to see if the visitor id exists
        if (visitor == null) {
            executor.sendMessage(this.getName() + ",invalid-visitor-id");
        }
        //makes sure the visitor's balance is not less that zero
        else if (visitor.getMoney().compareTo(BigDecimal.ZERO) < 1) {
            executor.sendMessage(this.getName() + ",outstanding-fine," + visitor.getMoney());
            return ResponseFlag.SUCCESS;
        }
        long amount = Long.parseLong(args[2]);


        return ResponseFlag.NOT_FINISHED;
    }
}
