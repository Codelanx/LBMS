package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.types.Visitor;
import edu.rit.codelanx.ui.Client;

import java.util.Optional;

/**
 * For simulation purposes. This method will advance the simulated date of the
 * library ahead by a specified number of days and/or hours. The total number
 * of days/hours advanced must be tracked by the system and added to the
 * current date as appropriate (e.g. to determine if books are overdue).
 * <p>
 * Request Format: advance,number-of-days[,number-of-hours];
 * number-of-days is the number of days to move the library's calendar
 * forward, must be between 0 and 7 days.
 * number-of-hours is the number of hours to move the library's calendar
 * forward, must be between 0 and 23 hours.
 */
public class AdvanceCommand extends TextCommand {

    /**
     * Constructor for the AdvanceCommand class
     *
     * @param server the server that the command is to be run on
     */
    public AdvanceCommand(Server server) {
        super(server);
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "advance";
    }

    /**
     * Whenever this command is called, it will simulate ahead to the chosen
     * date.
     *
     * @param executor  the client that is calling the command
     * @param arguments advance: name of the command to be run
     *                  numberofdays: number of days to move the library's
     *                  calendar forward, must be between 0 and 7 days.
     *                  numberofhours: number of hours to move the library's
     *                  calendar forward, must be between 0 and 23 hours.
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(Client executor, String... arguments) {
        long someID = 42;
        Optional<? extends Visitor> visitor = this.server.getDataStorage()
                .ofLoaded(Visitor.class)
                .filter(v -> v.getID() == someID)
                .findAny();
        return null;
    }
}
