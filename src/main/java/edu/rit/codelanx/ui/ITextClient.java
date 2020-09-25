package edu.rit.codelanx.ui;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.data.state.State;

import java.io.IOException;

public interface ITextClient extends Client, AutoCloseable{
    public void receive(Server server, TextMessage message) throws IOException;
}
