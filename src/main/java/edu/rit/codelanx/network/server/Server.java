package edu.rit.codelanx.network.server;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.network.io.Message;

public interface Server<T extends Message<?>> extends Messenger<T> {

    public Interpreter getInterpreter();
    public DataStorage getDataStorage();
    public Clock getClock();
}
