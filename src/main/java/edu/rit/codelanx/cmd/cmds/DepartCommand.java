package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.data.state.types.Visit;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.time.Instant;
import java.util.Optional;

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
     * @param arguments visitorID: the visitor whose visit will end
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... arguments) {

        //Checking that they have the correct amount of parameters
        if (numArgs(arguments, 1) == UtilsFlag.MISSINGPARAMS) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID;");
            return ResponseFlag.SUCCESS;
        }

        //Checking that the id passed was a number
        Long visitorID = checkVisitorID(arguments[0]);
        if (visitorID == -1) {
            return ResponseFlag.FAILURE;
        }

        //Finding the visitor with the matching ID in the database
        Visitor v = findVisitor(this.server, visitorID);
        if (v == null || !v.isVisiting()) {
            executor.sendMessage(this.getName() + ",invalid-id;");
        }
        
        Optional<? extends Visit> visit =
                server.getDataStorage().ofLoaded(Visit.class).filter(visit1 -> visit1.getVisitor() == v).findAny();
        executor.sendMessage(this.getName() + "," + v.getID() + "," + visit.get().toFormattedText());

        return ResponseFlag.SUCCESS;
    }
}
