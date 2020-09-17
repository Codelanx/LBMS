package edu.rit.codelanx.cmd;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.ui.Client;

public enum ResponseFlag implements Response {
    SUCCESS("Command ran successfully"),
    FAILURE("Command execution failed"),
    PARTIAL("Interpreter is waiting for a termination sequence"),
    NOT_FINISHED("Implementation of this command is not complete"),
    UNKNOWN("Unknown command"),
    ;

    private final Command<ResponseFlag> dummy = new DummyCommand();
    private final String info;

    private ResponseFlag(String info) {
        this.info = info;
    }

    public String getDescription() {
        return this.info;
    }

    public Command<? extends ResponseFlag> toDummyCommand() {
        return this.dummy;
    }

    private final class DummyCommand implements Command<ResponseFlag> {

        @Override
        public String getName() {
            return "undefined";
        }

        @Override
        public ResponseFlag onExecute(Server ranOn, Client executor, String... arguments) {
            return ResponseFlag.this;
        }
    }
}
