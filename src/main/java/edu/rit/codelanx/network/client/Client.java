package edu.rit.codelanx.network.client;

import edu.rit.codelanx.network.io.Message;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;

import java.io.IOException;

public interface Client<T extends Message<?>> extends CommandExecutor, Messenger<T>, AutoCloseable {

    public void connect(Server<T> server);

    public void display() throws IOException;

}
