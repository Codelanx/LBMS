package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Visit;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.time.Duration;
import java.time.Instant;

import static edu.rit.codelanx.cmd.CommandUtils.*;

/**
 * Ends a visit in progress.
 * <p>
 * Request Format: depart,visitor ID
 * visitor ID is the unique 10-digit ID of the visitor
 */
public class DepartCommand extends TextCommand {

    /**
     * Constructor for the DepartCommand class
     *
     * @param server the server that the command is to be run on
     */
    public DepartCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return null;
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "depart";
    }

    /**
     * Whenever this command is called, it will end the visit of the visitor
     * whose id is specified.
     *
     * @param executor  the client that is calling the command
     * @param args visitorID: the visitor whose visit will end
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... args) {

        //Checking that they have the correct amount of parameters
        if (numArgs(args, 1) == UtilsFlag.MISSINGPARAMS) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID;");
            return ResponseFlag.SUCCESS;
        }

        //Checking that the id passed was a number
        Long visitorID = checkVisitorID(args[0]);
        if (visitorID == -1) {
            return ResponseFlag.FAILURE;
        }

        //Finding the visitor with the matching ID in the database
        Visitor v = findVisitor(this.server, visitorID);
        if (v == null || !v.isVisiting()) {
            executor.sendMessage(this.getName() + ",invalid-id;");
            return ResponseFlag.SUCCESS;
        }
        Instant start = v.getVisitStart();
        Instant end = Instant.now();
        Duration d = Duration.between(start, end);
        Visit result = v.endVisit(end);

        String endOutput = TIME_OF_DAY_FORMAT.format(end);
        String durOutput = this.formatDuration(d);

        executor.sendMessage(this.buildResponse(this.getName(), v.getID() + "", endOutput, durOutput));

        return ResponseFlag.SUCCESS;
    }
}
