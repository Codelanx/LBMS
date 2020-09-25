package edu.rit.codelanx.cmd;

public interface Interpreter {

    public void receive(CommandExecutor executor, String data);
}
