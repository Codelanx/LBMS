package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.UtilsFlag;
import static edu.rit.codelanx.cmd.CommandUtils.*;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.types.Visitor;
import edu.rit.codelanx.ui.Client;
import jdk.internal.net.http.common.Pair;

import java.util.Optional;

import static edu.rit.codelanx.cmd.CommandUtils.numArgs;
import static java.lang.Long.parseLong;

/**
 * Begins a new visit by a registered visitor.
 * <p>
 * Request Format: 	arrive,visitor ID;
 * visitor ID is the unique 10-digit ID of the visitor.
 */
public class ArriveCommand extends TextCommand {

    /**
     * Constructor for the ArriveCommand class
     *
     * @param server the server that the command is to be run on
     */
    public ArriveCommand(Server server) {
        super(server);
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "arrive";
    }

    /**
     * Whenever this command is called, it will begin a new visit.
     *
     * @param executor  the client that is calling the command
     * @param arguments visitorID: the unique 10-digit ID of the visitor
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... arguments) {
        //Checking that they have the correct amount of parameters
        if (numArgs(arguments, 1) == UtilsFlag.MISSINGPARAMS) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID;");
            return ResponseFlag.SUCCESS;
        }

        //Checking that the id passed was a number
        Long visitorID = checkVisitorID(arguments[0]);
        if (visitorID == null){
            return ResponseFlag.FAILURE;
        }

        //Finding the visitor with the matching ID in the database
        Visitor v = findVisitor(this.server, visitorID);
        if (v == null){
            executor.sendMessage(this.getName() + ",invalid-id;");
        }

        //TODO: Start a visit for the visitor

        return ResponseFlag.NOT_FINISHED;
    }
}
