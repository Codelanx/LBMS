package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public abstract class TextCommand implements Command {

    public static final String TOKEN_DELIMITER = ",";
    public static final DateTimeFormatter TIME_OF_DAY_FORMAT;
    public static final DateTimeFormatter DATE_FORMAT;
    protected final Server<TextMessage> server;
    private final String usage;
    final TextParam[] params;

    static {
        TIME_OF_DAY_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withLocale( Locale.US )
                        .withZone( ZoneId.systemDefault());
        DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy:MM:dd")
                .withLocale( Locale.US )
                .withZone( ZoneId.systemDefault());
    }

    public TextCommand(Server<TextMessage> server) {
        this.server = server;
        TextParam.Builder builder = this.buildParams();
        if (builder == null) {
            //TODO: Fix this once implemented
            this.params = null;
            this.usage = "";
        } else {
            this.params = builder.build();
            this.usage = builder.buildString();
        }
    }

    /**
     * Takes a list of tokens to put into a response, delimited by
     * {@link TextCommand#TOKEN_DELIMITER}
     *
     * @param tokens The set of string tokens to join together
     * @return The input arguments joined together by a common delimiter
     */
    protected String buildResponse(Object... tokens) {
        return String.join(TOKEN_DELIMITER, Arrays.stream(tokens).map(Objects::toString).toArray(String[]::new)) + ";";
    }

    /**
     * Provides a builder of the expected command arguments for this command
     *
     * @return A new {@link TextParam#create()} call with expected parameters
     * @see TextParam
     * @see TextParam.Builder
     */
    protected abstract TextParam.Builder buildParams();

    @Override
    public String getUsage() {
        return this.usage;
    }

    protected String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutes() - (hours * 60);
        long seconds = d.getSeconds() - (d.toMinutes() * 60);
        return String.format("%d:%d:%d", hours, minutes, seconds);
    }
}
