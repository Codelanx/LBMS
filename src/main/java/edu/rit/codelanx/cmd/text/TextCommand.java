package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.StreamSupport;

/*
TODO:
TODO:   For every command, test for:
TODO:     - No input
TODO:     - Bad input
TODO:     - Good input
TODO:     - Other weird bad inputs / edge cases (e.g. duplicates)
TODO:
 */

/**
 * Represents a {@link Command} which acts on text/{@link String} arguments
 *
 * @author sja9291  Spencer Alderman
 * @see <a href="http://www.se.rit.edu/~swen-262/projects/design_project/ProjectDescription/LBMS-client-request-format.html">
 *          LBMS Command Specification
 *      </a>
 */
public abstract class TextCommand implements Command {

    /** The delimiter for string tokens in a command input */
    public static final String TOKEN_DELIMITER = ",";
    /** The {@link DateTimeFormatter} for hh:mm:ss format */
    public static final DateTimeFormatter TIME_OF_DAY_FORMAT;
    /** The {@link DateTimeFormatter} for yyyy/mm/dd format */
    public static final DateTimeFormatter DATE_FORMAT;
    /** The {@link Server} this command is run on */
    protected final Server<TextMessage> server;
    //The usage string, minus #getName
    private final String usage;
    /** Our {@link TextParam parameters} for command inputs, to verify inputs */
    final TextParam[] params;

    static {
        //initialize our formatters
        TIME_OF_DAY_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withLocale(Locale.US)
                        .withZone(ZoneId.systemDefault());
        DATE_FORMAT = new DateTimeFormatterBuilder()
                .appendPattern("yyyy/MM/dd")
                .parseDefaulting(ChronoField.NANO_OF_DAY, 0) //for parsing dates to instants
                .toFormatter()
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault());
    }

    /**
     * Initializes the parameter list for this command
     *
     * @param server The {@link Server} this command runs on
     */
    public TextCommand(Server<TextMessage> server) {
        this.server = server;
        //Grab the builder
        TextParam.Builder builder = this.buildParams();
        if (builder == null) { //used to be null before implementing them all
            throw new IllegalStateException("Did not implement #buildParams correctly - cannot return null");
        }
        this.params = builder.build();
        this.usage = builder.buildString();
    }

    //REFACTOR: DRY the below 4 methods

    /**
     * Takes a list of tokens to put into a response, delimited by
     * {@link TextCommand#TOKEN_DELIMITER}. For example:
     * {@code buildResponse(1, 2, 3) == "1,2,3;"}
     *
     * @param tokens The set of string tokens to join together
     * @return The input arguments joined together by a common delimiter
     */
    protected String buildResponse(Object... tokens) {
        return String.join(TOKEN_DELIMITER, Arrays.stream(tokens).map(Objects::toString).toArray(String[]::new)) + ";";
    }

    /** @see #buildResponse(Object...) */
    protected String buildResponse(Iterable<?> tokens) {
        return String.join(TOKEN_DELIMITER, StreamSupport.stream(tokens.spliterator(), false).map(Objects::toString).toArray(String[]::new)) + ";";
    }


    /**
     * Takes a list of tokens to put into a response in list form, delimited by
     * {@link TextCommand#TOKEN_DELIMITER}. For example:
     * {@code buildListResponse(1, 2, 3) == "{1,2,3}"}
     *
     * @param tokens The set of string tokens to join together
     * @return The input arguments joined together by a common delimiter
     */
    protected String buildListResponse(Object... tokens) {
        return "{" + String.join(TOKEN_DELIMITER, Arrays.stream(tokens).map(Objects::toString).toArray(String[]::new)) + "}";
    }

    /** @see #buildListResponse(Object...) */
    protected String buildListResponse(Iterable<?> tokens) {
        return "{" + String.join(TOKEN_DELIMITER, StreamSupport.stream(tokens.spliterator(), false).map(Objects::toString).toArray(String[]::new)) + "}";
    }

    /**
     * Provides a builder of the expected command arguments for this command
     *
     * @return A new {@link TextParam#create()} call with expected parameters
     * @see TextParam
     * @see TextParam.Builder
     */
    protected abstract TextParam.Builder buildParams();

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getUsage() {
        return this.usage;
    }

    /**
     * Formats a given {@link Duration} into hh:mm:ss format, where the hours
     * portion may exceed 24 as necessary
     *
     * @param d The {@link Duration} to format
     * @return The output of the formatting, e.g. "999:59:59" or "12:34:56"
     */
    protected String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutes() - (hours * 60);
        long seconds = d.getSeconds() - (d.toMinutes() * 60);
        return String.format("%.2d:%.2d:%.2d", hours, minutes, seconds);
    }
}
