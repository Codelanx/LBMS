package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
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
import java.util.HashMap;
import java.util.Map;


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
        return TextParam.create().argument("visitor-id");
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
     * @author maa1675  Mark Anderson
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... args) {

        Long id = InputOutput.parseLong(args[0]).orElse(null);
        if (id == null) {
            return ResponseFlag.FAILURE;
        }
        //pre: we have a valid id, we need a Visitor
        Visitor visitor = this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, id)
                .results().findAny().orElse(null);
        if (visitor == null || !visitor.isVisiting()) {
            executor.sendMessage(buildResponse(this.getName(),"invalid-id"));
            return ResponseFlag.SUCCESS;
        }

        Visit visit = visitor.endVisit(Instant.now());

        Duration d = Duration.between(visit.getStart(), visit.getEnd());

        String endOutput = TIME_OF_DAY_FORMAT.format(visit.getEnd());
        String durOutput = this.formatDuration(d);

        executor.sendMessage(this.buildResponse(this.getName(), visitor.getID(),
                endOutput, durOutput));

        return ResponseFlag.SUCCESS;
    }
}
