package edu.rit.codelanx.ui;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.io.IOException;

public interface OldITextClient extends OIdClient, AutoCloseable{
    public void receive(Server server, TextMessage message) throws IOException;
}
