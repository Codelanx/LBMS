package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Visitor;
import com.codelanx.commons.util.InputOutput;

import java.util.HashMap;
import java.util.Map;

/**
 * Begins a new visit by a registered visitor.
 * <p>
 * Request Format: 	arrive,visitor ID;
 * visitor ID is the unique 10-digit ID of the visitor.
 * @author maa1675  Mark Anderson
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
        return TextParam.create()
                .argument("visitor-id");
    }

    /**
     * @see Command#getName
     */
    @Override
    public String getName() {
        return "arrive";
    }

    /**
     * {@inheritDoc}
     * @param executor  {@inheritDoc}
     * @param args      {@inheritDoc}
     *                  args[0]: visitorID
     * @return {@inheritDoc}
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... args) {

        if (!this.server.getLibraryData().getLibrary().isOpen()){
            executor.sendMessage(buildResponse(this.getName(), "library-is-closed"));
            return ResponseFlag.FAILURE;
        }

        boolean incorrectArgs = false;
        Map<Integer, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i++){
            if (args[i].isEmpty()) {
                argMap.put(i, this.params[i].getLabel());
                if (i == 0) {
                    incorrectArgs = true;
                }
            }
        }

        if (incorrectArgs){
            String response = "";
            for (Map.Entry<Integer,String> entry : argMap.entrySet()){
                response += this.params[entry.getKey()];
            }
            executor.sendMessage(this.buildResponse(this.getName(),
                    "missing-parameters", response));
            return ResponseFlag.SUCCESS;
        }

        Long id = InputOutput.parseLong(args[0]).orElse(null);
        if (id == null) {
            return ResponseFlag.FAILURE;
        }
        return this.execute(executor, id);
    }

    /**
     * Whenever this command is called, it will begin a new visit.
     *
     * @param executor the client that is calling the command
     * @param visitorID the unique 10-digit ID of the visitor
     * @return a {@link ResponseFlag} that says whether or not the command was
     * executed correctly
     */
    public ResponseFlag execute(CommandExecutor executor, long visitorID) {
        //pre: we have a valid id, we need a Visitor
        Visitor visitor = getVisitor(visitorID);
        if (visitor == null) {
            executor.sendMessage(buildResponse(this.getName(), "invalid-id"));
            return ResponseFlag.SUCCESS;
        } else if (visitor.isVisiting()) {
            executor.sendMessage(buildResponse(this.getName(), "duplicate"));
            return ResponseFlag.SUCCESS;
        }
        executor.sendMessage(startVisit(visitor));
        return ResponseFlag.SUCCESS;
    }

    /**
     * startVisit is a helper method for execute that starts a visit with a visitor
     * @param visitor the visitor to start the visit for
     * @return the response string to be output
     */
    protected String startVisit(Visitor visitor) {
        visitor.startVisit(this.server.getLibraryData().getLibrary());
        return buildResponse(this.getName(), visitor.getID(),
                TIME_OF_DAY_FORMAT.format(server.getClock().getCurrentTime()));
    }

    /**
     * getVisitor is a helper method for {@link #onExecute}  that gets a visitor from our database
     * @param visitorID the {@link Visitor} to get from the database
     * @return the {@link Visitor} that was found, or null if none found
     */
    protected Visitor getVisitor(long visitorID){
        return this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.ID, visitorID)
                .results().findAny().orElse(null);
    }
}
