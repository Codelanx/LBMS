package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.*;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.rit.codelanx.cmd.CommandUtils.numArgs;

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
    public ReportCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return null;
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
     * @param args report: name of the command to be run
     *                  days: the number of days that the report should cover
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        if (numArgs(args, 1) == UtilsFlag. MISSINGPARAMS) {
            executor.sendMessage(this.getName() + ",missing-parameters");
            return ResponseFlag.SUCCESS;
        }

        else {


            //Gets the date
            executor.sendMessage(getName() + "," + server.getClock().getCurrentTime() + ";");

            //Finds all the books
            Book books = this.server.getDataStorage().ofLoaded(Book.class).findAny().orElse(null);
            executor.renderState(books);

            //TODO fix
            Visitor numVisitors = this.server.getDataStorage().ofLoaded(Visitor.class).findAny().orElse(null);
            executor.renderState(numVisitors);

            // Gets the duration of all the visits
            Visit avgVisit = this.server.getDataStorage().ofLoaded(Visit.class).findAny().orElse(null);
            executor.sendMessage(avgVisit.getDuration().toString());

            //TODO get the number of books purchased
            Library numPurchased = this.server.getDataStorage().ofLoaded(Library.class).findAny().orElse(null);
            //executor.sendMessage();


            Visitor getTotalFines = this.server.getDataStorage().ofLoaded(Visitor.class).findAny().orElse(null);


            Map<String, Set<Transaction>> map = this.server.getDataStorage().query(Transaction.class)
                    .results()
                    .collect(Collectors.groupingBy(Transaction::getReason, Collectors.toSet()));
            Set<Transaction> lateFees = map.get("late fee");
            Set<Transaction> paidFees = map.get("Paying off balance");
            int finesCollected = paidFees.size();
            int outstandingFines = lateFees.size() - finesCollected;

            return ResponseFlag.NOT_FINISHED;

        }

    }
}
