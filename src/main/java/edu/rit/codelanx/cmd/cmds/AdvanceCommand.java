package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.HashMap;
import java.util.Map;


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
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @see edu.rit.codelanx.cmd.Command#getName()
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
        boolean incorrectArgs = false;
        Map<Integer, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i++){
            if (args[i].isEmpty()) {
                argMap.put(i, this.params[i].getLabel());
                if (i == 0){
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
                    "missing-parameters",response));
            return ResponseFlag.SUCCESS;
        }

        //Checking the hours and days passed in to make sure they are within
        int days = InputOutput.parseInt(args[0]).orElse(-1);
        int hours = InputOutput.parseInt(args[1]).orElse(-1);
        return this.executeInternal(executor, days, args[0], hours, args[1]);
    }

    public ResponseFlag execute(CommandExecutor executor, int days, int hours) {
        return this.executeInternal(executor, days, days + "", hours, hours + "");
    }

    private ResponseFlag executeInternal(CommandExecutor executor,
                                    int days, String daysArg,
                                    int hours, String hoursArg) {
        if (days < 0 || days > 7) {
            executor.sendMessage(this.buildResponse(this.getName(), "invalid-number-of-days", daysArg));
            return ResponseFlag.SUCCESS;
        }
        if (hours < 0 || hours > 23) {
            executor.sendMessage(this.buildResponse(this.getName(), "invalid-number-of-hours", hoursArg));
            return ResponseFlag.SUCCESS;
        }
        this.server.getClock().advanceTime(days, hours);
        executor.sendMessage(buildResponse(this.getName(),"success;"));
        return ResponseFlag.SUCCESS;
    }
}
