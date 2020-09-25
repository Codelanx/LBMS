package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

/**
 * Displays the current date and time in the simulation. This should include
 * any days that have been added to the calendar using the command to advance
 * time.
 * <p>
 * Request Format: datetime
 */
public class DatetimeCommand extends TextCommand {

    /**
     * Constructor for the DatetimeCommand class
     *
     * @param server the server that the command is to be run on
     */
    public DatetimeCommand(Server server) {
        super(server);
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "datetime";
    }

    /**
     * Whenever this command is called, it will display the current time and
     * date that the sim is currently in.
     *
     * @param executor  the client that is calling the command
     * @param arguments datetime: name of the command to be run
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... arguments) {
        return ResponseFlag.NOT_FINISHED;
    }
}
