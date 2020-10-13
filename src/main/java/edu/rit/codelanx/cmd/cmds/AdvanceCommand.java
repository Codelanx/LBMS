package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

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
 *
 * @author maa1675  Mark Anderson
 * @author sja9291  Spencer Alderman    refactoring
 */
public class AdvanceCommand extends TextCommand {

    /**
     * Constructor for the AdvanceCommand class
     *
     * @param server the server that the command is to be run on
     */
    public AdvanceCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("number-of-days")
                .argumentOptional("number-of-hours");
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
     * @param executor the client that is calling the command
     * @param args     numberofdays: number of days to move the library's
     *                 calendar forward, must be between 0 and 7 days.
     *                 numberofhours: number of hours to move the library's
     *                 calendar forward, must be between 0 and 23 hours.
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        //Checking that they have the correct amount of parameters
        if (args.length < 1 || args.length > 2) {
            executor.sendMessage(this.buildResponse(this.getName(), "missing" +
                    "-parameters", "numberofdays" , "numberofhours"));
            return ResponseFlag.SUCCESS;
        }

        //Checking the hours and days passed in to make sure they are within
        Optional<Integer> days = InputOutput.parseInt(args[0]);
        Optional<Integer> hours = InputOutput.parseInt(args[1]);
        if (!days.isPresent() || days.get() < 0 || days.get() > 7) {
            executor.sendMessage(this.buildResponse(this.getName(), "invalid-number-of-days", args[0]));
            return ResponseFlag.SUCCESS;
        }
        if (!args[1].isEmpty() && (!hours.isPresent() || hours.get() < 0 || hours.get() > 23)) {
            executor.sendMessage(this.buildResponse(this.getName(), "invalid-number-of-hours", args[1]));
            return ResponseFlag.SUCCESS;
        }
        this.server.getClock().advanceTime(days.get(), hours.orElse(0));
        executor.sendMessage(this.getName() + "success;");
        return ResponseFlag.SUCCESS;
    }
}
