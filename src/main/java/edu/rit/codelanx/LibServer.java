package edu.rit.codelanx;

import edu.rit.codelanx.data.DataFacade;
import edu.rit.codelanx.data.DataStorage;

public class LibServer implements Server {

    private final DataStorage storage; //stores data
    private final Clock clock; //passes the time

    /**
     * Starts our program, loads relevant data, begins ticking server logic, etc
     */
    public LibServer() {
        this.storage = new DataFacade();
        this.clock = new Clock(this);
    }

    @Override
    public DataStorage getDataStorage() {
        return this.storage;
    }

}
