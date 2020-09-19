package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.ui.Client;

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
     * TODO: Figure out linking to Command
     */
    @Override
    public String getName() {
        return "arrive";
    }

    /**
     * Whenever this command is called, it will begin a new visit.
     *
     * @param executor  the client that is calling the command
     * @param arguments arrive: name of the command to be run
     *                  visitorID: the unique 10-digit ID of the visitor
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(Client executor, String... arguments) {
        return null;
    }
}
