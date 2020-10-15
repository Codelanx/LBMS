package edu.rit.codelanx.network.server;

import edu.rit.codelanx.cmd.MessengerExecutor;
import edu.rit.codelanx.data.loader.BookStoreAdapter;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.text.TextInterpreter;
import edu.rit.codelanx.data.LibraryData;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;

/**
 * Our concrete server class, which holds the relevant subsystems and receives
 * messages from {@link Messenger clients} who have connected to it
 *
 * @author sja9291  Spencer Alderman
 */
public class TextServer implements Server<TextMessage> {

    private final DataSource library; //stores the library's data
    private final DataSource bookStore; //stores the book store's data
    private final Clock clock; //passes the time
    private final Interpreter commands; //the command interpreter

    /**
     * Starts our program, loads relevant data, begins ticking server logic, etc
     */
    public TextServer() {
        this.bookStore = new LibraryData(BookStoreAdapter::new);
        this.library = new LibraryData();
        this.commands = new TextInterpreter(this);
        this.clock = new Clock(this);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Interpreter getInterpreter() {
        return this.commands;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataSource getLibraryData() {
        return this.library;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataSource getBookStore() {
        return this.bookStore;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Clock getClock() {
        return this.clock;
    }

    /**
     * {@inheritDoc}
     * @param from The {@link Messenger Messenger&lt;TextMessage&gt;} who sent the
     *             {@code message}
     * @param message The received {@link TextMessage TextMessage}
     */
    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {
        String data = message.getData();
        this.getInterpreter().receive(new MessengerExecutor(this, from), data);
    }
}
