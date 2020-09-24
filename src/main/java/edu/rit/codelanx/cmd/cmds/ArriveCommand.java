package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.types.Visitor;
import edu.rit.codelanx.ui.Client;

import java.util.Optional;

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
    public ResponseFlag onExecute(Client executor, String... arguments) {
        //Checking that they have the correct amount of parameters
        if (arguments.length < 1) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID;");
            return ResponseFlag.SUCCESS;
        } else if (arguments.length > 2) {
            return ResponseFlag.FAILURE;
        }

        //Checking that the id passed was a number
        long visitorID;
        try {
            visitorID = parseLong(arguments[0]);
        } catch (NumberFormatException n) {
            return ResponseFlag.FAILURE;
        }

        //Finding the visitor with the matching ID in the database
        Optional<? extends Visitor> visitorSearch = this.server.getDataStorage()
                .ofLoaded(Visitor.class)
                .filter(v -> v.getID() == visitorID)
                .findAny();

        //Seeing if the search found a visitor
        if (visitorSearch.isPresent()) {
            Visitor visitor = visitorSearch.get();
            //TODO: Check if the visitor is currently in a visit, if not,
            // start a visit
        } else {
            executor.sendMessage(this.getName() + ",invalid-id");
            return ResponseFlag.SUCCESS;
        }

        return ResponseFlag.NOT_FINISHED;
    }
}
