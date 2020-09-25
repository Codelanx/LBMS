package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

/**
 * Reports various statistics about library usage for a period covering a
 * specified number of days.
 * <p>
 * Request Format: report[,days]
 * days is the number of days that the report should include in its
 * statistics. If omitted the report should include statistics using all data
 * collecting since the beginning of the simulation.
 */
public class ReportCommand extends TextCommand {

    /**
     * Constructor for the ReportCommand class
     *
     * @param server the server that the command is to be run on
     */
    public ReportCommand(Server server) {
        super(server);
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "report";
    }

    /**
     * Whenever this command is called, it will return a report about the
     * usage of the library over a set period of time.
     *
     * @param executor  the client that is calling the command
     * @param arguments report: name of the command to be run
     *                  days: the number of days that the report should cover
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... arguments) {
        return ResponseFlag.NOT_FINISHED;
    }
}
