package edu.rit.codelanx.cmd;

import edu.rit.codelanx.network.io.Message;

/**
 * A flag which indicates the result of executing a given string of input
 * through an {@link Interpreter}
 *
 * @author sja9291  Spencer Alderman
 */
public enum ResponseFlag implements Message<String> {
    /**
     * The {@link Command} handled the input appropriately, including handling
     * erroneous input accordingly
     */
    SUCCESS("Command ran successfully"),
    /** The {@link Command} failed to run normally e.g. an exception occurred */
    FAILURE("Command execution failed"),
    /** The interpreter received a partial request and is awaiting input */
    PARTIAL("Interpreter is waiting for a termination sequence"),
    /** The interpreter received a request, but it was missing arguments */
    MISSING_ARGS("Command did not receive all necessary arguments"),
    /**
     * The interpreter received a request for an unfinished / unimplemented
     * feature or command
     */
    NOT_FINISHED("Implementation of this command is not complete"),
    UNKNOWN("Unknown command"),
    ;

    private final Command dummy = new DummyCommand();
    private final String info;

    private ResponseFlag(String info) {
        this.info = info;
    }

    public String getDescription() {
        return this.info;
    }

    public Command toDummyCommand() {
        return this.dummy;
    }

    @Override
    public String getData() {
        return this.info;
    }

    private final class DummyCommand implements Command {

        @Override
        public String getName() {
            return "undefined";
        }

        @Override
        public String getUsage() {
            return "";
        }

        @Override
        public ResponseFlag onExecute(CommandExecutor executor, String... args) {
            return ResponseFlag.this;
        }
    }
}
