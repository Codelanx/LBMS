package edu.rit.codelanx.cmd;

import edu.rit.codelanx.network.io.Message;

public enum ResponseFlag implements Message<String> {
    SUCCESS("Command ran successfully"),
    FAILURE("Command execution failed"),
    PARTIAL("Interpreter is waiting for a termination sequence"),
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
        public ResponseFlag onExecute(CommandExecutor executor, String... args) {
            return ResponseFlag.this;
        }
    }
}
