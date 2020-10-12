package edu.rit.codelanx.network.server;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.network.io.Message;

import edu.rit.codelanx.data.state.types.Library; //imported solely for javadocs

/**
 * Represents an instance which many {@link Messenger} objects can connect to
 * in order to interact with the services provided by our system
 *
 * @param <T> The {@link Message<T>} used to communicate with this server
 *
 * @author sja9291  Spencer Alderman
 */
public interface Server<T extends Message<?>> extends Messenger<T> {

    /**
     * Returns the {@link Interpreter}, which decides how to act
     * upon a received {@link Message<T>}
     *
     * @return The held {@link Interpreter} for this server
     * @see Interpreter
     */
    public Interpreter getInterpreter();

    /**
     * Returns the {@link DataStorage} which holds all of the library's
     * data, relevant to customers and books owned within the library
     *
     * @return A {@link DataStorage} responsible for holding the library's
     *         current data
     * @see DataStorage
     */
    public DataStorage getDataStorage();

    /**
     * Returns the {@link DataStorage} which refences the "Book Store", aka
     * an emulated online shop which you can query in a similar fashion to
     * {@link #getDataStorage()}, and which provides the available books to
     * be added to a {@link Library}, as they are represented in the
     * {@code books.txt} resource
     *
     * @return A {@link DataStorage} responsible for parsing and providing
     *         references to available data to add to our {@link Library}
     * @see DataStorage
     */
    public DataStorage getBookStore();
    public Clock getClock();
}
