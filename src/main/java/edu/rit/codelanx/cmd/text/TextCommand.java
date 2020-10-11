package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;

public abstract class TextCommand implements Command {

    public static final String TOKEN_DELIMITER = ",";
    protected final Server<TextMessage> server;
    private final String usage;
    final TextParam[] params;

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
    protected String buildResponse(String... tokens) {
        return String.join(TOKEN_DELIMITER, tokens) + ";";
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
}
