package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.util.Clock;

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
    public DatetimeCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create(); //no args
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
     * @param args none
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor,
                                  String... args) {

        //Checking that no other arguments were passed in
        if (args.length > 0) {
            return ResponseFlag.FAILURE;
        }
        //Getting the current time from the server's clock
        executor.sendMessage(this.getName() + "," + server.getClock().getCurrentTime() + ";");

        return ResponseFlag.SUCCESS;
    }
}
