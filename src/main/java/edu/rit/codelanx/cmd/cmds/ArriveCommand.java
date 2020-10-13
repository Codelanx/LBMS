package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Visitor;
import com.codelanx.commons.util.InputOutput;

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
    public ArriveCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create().argument("visitor-id");
    }

    /**
     * @see edu.rit.codelanx.cmd.Command#getName
     */
    @Override
    public String getName() {
        return "arrive";
    }

    /**
     * Whenever this command is called, it will begin a new visit.
     *
     * @param executor the client that is calling the command
     * @param args     visitorID: the unique 10-digit ID of the visitor
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... args) {
        if (!this.server.getLibraryData().getLibrary().isOpen()){
            executor.sendMessage(this.getName() + "," + "library-closed;");
            return ResponseFlag.FAILURE;
        }
        if (args.length != 1) {
            executor.sendMessage(this.getName() + "," + "missing-parameters," +
                    "visitorID");
            return ResponseFlag.SUCCESS;
        }
        Long id = InputOutput.parseLong(args[0]).orElse(null);
        if (id == null) {
            return ResponseFlag.FAILURE;
        }
        //pre: we have a valid id, we need a Visitor
        Visitor visitor = this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, id)
                .results().findAny().orElse(null);
        if (visitor == null) {
            executor.sendMessage(this.getName() + ",invalid-id;");
            return ResponseFlag.SUCCESS;
        } else if (visitor.isVisiting()) {
            executor.sendMessage(this.getName() + ",duplicate;");
            return ResponseFlag.SUCCESS;
        }

        boolean visit = visitor.startVisit(this.server.getLibraryData().getLibrary());
        executor.sendMessage(this.getName() + "," + visitor.getID() + "," +
                TIME_OF_DAY_FORMAT.format(server.getClock().getCurrentTime()) + ";");
        return ResponseFlag.SUCCESS;
    }
}
