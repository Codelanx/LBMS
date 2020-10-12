package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandUtils;
import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.ArrayList;
import java.util.List;

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
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "numberofdays,numberofhours;");
            return ResponseFlag.SUCCESS;
        }

        //Checking the hours and days passed in to make sure they are within
        // their parameters
        int daysInt;
        int hoursInt;
        try {
            daysInt = Integer.parseInt(args[0]);
            if (daysInt > 7 || daysInt < 0){
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e){
            executor.sendMessage(this.getName() + ",invalid-number-of" +
                    "-days," + args[0]);
            return ResponseFlag.SUCCESS;
        }

        //Seeing if they gave us days and hours or just hours
        if (args.length == 2) {
            try {
                hoursInt = Integer.parseInt(args[1]);
                if (hoursInt > 23 || hoursInt < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                executor.sendMessage(this.getName() + ",invalid-number-of" +
                        "-hours," + args[1]);
                return ResponseFlag.SUCCESS;
            }
        } else {
            hoursInt = 0;
        }
        this.server.getClock().advanceTime(daysInt, hoursInt);
        executor.sendMessage(this.getName() + "success;");
        return ResponseFlag.SUCCESS;
    }
}
