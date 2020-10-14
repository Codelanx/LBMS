package edu.rit.codelanx.network.client;

import edu.rit.codelanx.network.io.Message;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.server.Server;

import java.io.IOException;

/**
 * Provides an interface for a user to interact with our system
 *
 * @param <T> The type of messages being exchanged
 *
 * @author sja9291  Spencer Alderman
 */
public interface Client<T extends Message<?>> extends Messenger<T>, AutoCloseable {

    /**
     * Begins an interactive session with a specified {@code server},
     * until the client exits the program
     *
     * @param server The {@link Server} to connect to and interact with
     */
    public void connect(Server<T> server);

    /**
     * Begins the interactive session
     *
     * @throws IOException If an error occurs while interacting with the user
     */
    public void display() throws IOException;

}
