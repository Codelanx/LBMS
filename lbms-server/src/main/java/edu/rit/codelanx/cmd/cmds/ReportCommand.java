package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextCommand;
import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Transaction;
import edu.rit.codelanx.data.state.types.Visit;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reports various statistics about library usage for a period covering a
 * specified number of days.
 * <p>
 * Request Format: report[,days]
 * days is the number of days that the report should include in its
 * statistics. If omitted the report should include statistics using all data
 * collecting since the beginning of the simulation.
 *
 * @author cb4501 Connor Bonitati
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
        return TextParam.create()
                .argumentOptional("days");
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "report";
    }

    /**
     * {@inheritDoc}
     * @param executor  {@inheritDoc}
     * @param args      {@inheritDoc}
     *                  args[0]: days
     * @return {@inheritDoc}
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {

        // TODO: Days can be obmitted, you just use all the data collected since the start of the sim
        Optional<Long> days = InputOutput.parseLong(args[0]);

        if (!days.isPresent()) {
            executor.sendMessage("report,invalid-argument");
            return ResponseFlag.SUCCESS;
        }

        return this.execute(executor, Math.toIntExact(days.get()));
    }

    /**
     * Whenever this command is called, it will return a report about the
     * usage of the library over a set period of time.
     *
     * @param executor the client that is calling the command
     * @param days: the number of days that the report should cover
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    public ResponseFlag execute(CommandExecutor executor, int days) {
        //Gets the date
        Instant curDate = this.server.getClock().getCurrentTime();

        //Finds all the books
        long books = getBookCount();

        // Gathers the number of visitors registered
        long numVisitors = getNewVisitorCount();


        //Uses summary statistics to get the average time of visits
        double average = getAverageVisitLength(); //average duration of a visit
        Duration avg = Duration.ofSeconds((long)average);
        String avgOutput = this.formatDuration(avg);


        // Counts the number of books purchased
        long numPurchased = getBooksPurchasedAmount();

        Map<String, Set<Transaction>> map = getTransactions();

        int amountLateFees =
                map.getOrDefault(Transaction.Reason.CHARGING_LATE_FEE.getReason(), Collections.emptySet()).size();
        int amountPaidFees =
                map.getOrDefault(Transaction.Reason.PAYING_LATE_FEE.getReason(), Collections.emptySet()).size();
        int outstandingFines = amountLateFees - amountPaidFees;

        executor.sendMessage(getName()
                + "Date Generated: " + DATE_FORMAT.format(curDate)
                + "\n Number of Books: " + books
                + "\n Number of Visitors: " + numVisitors
                + "\n Average Length of Visit: " + avgOutput
                + "\n Number of Books Purchased: " + numPurchased //test
                + "\n Fines Collected: " + amountPaidFees
                + "\n Fines Outstanding: " + outstandingFines);


        return ResponseFlag.SUCCESS;

    }

    /**
     * getBookCount is a helper method for {@link #onExecute} that gets the amount of books in the library
     * @return the amount of books as a long
     */
    protected Long getBookCount(){
        return this.server.getLibraryData().query(Book.class)
                .results()
                .count();
    }

    /**
     * getNewVisitorCount is a helper method for {@link #onExecute} that gets the amount of new visitors registered
     * @return the amount of visitors
     */
    protected Long getNewVisitorCount(){
        return this.server.getLibraryData().query(Visitor.class)
                .results()
                .count();
    }

    /**
     * getAverageVisitLength is a helper method for {@link #onExecute} that gets the average of the length of the visits
     * @return the average visit length as a double
     */
    protected Double getAverageVisitLength(){
        return this.server.getLibraryData().query(Visit.class) //Query<Visit>
                        .results() //Stream<Visit>
                        .map(visit -> Duration.between(visit.getStart(),
                                visit.getEnd())) //Stream<Duration>
                        .mapToLong(Duration::getSeconds)//Stream<Long>
                        .summaryStatistics().getAverage();
    }

    /**
     * getBooksPurchasedAmount is a helper method for {@link #onExecute} that gets the amount of books purchased
     * @return the amount of books as a long
     */
    protected long getBooksPurchasedAmount(){
        return this.server.getLibraryData().query(Book.class)
                .results()
                .map(Book::getTotalCopies)
                .reduce(0, Integer::sum);
    }

    /**
     * getTransactions is a helper method for {@link #onExecute} that gets the transactions that are in the database
     * @return the transactions as a map of the reason and the transaction object
     */
    protected Map<String, Set<Transaction>> getTransactions(){
        return this.server.getLibraryData()
                .query(Transaction.class)
                .results()
                .collect(Collectors.groupingBy(Transaction::getReason, Collectors.toSet()));
    }
}
