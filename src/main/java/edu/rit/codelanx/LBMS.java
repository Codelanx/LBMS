package edu.rit.codelanx;

import com.codelanx.commons.logging.Debugger;
import com.codelanx.commons.logging.Logging;
import edu.rit.codelanx.network.client.Client;
import edu.rit.codelanx.network.client.TextClient;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.TextServer;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.util.Errors;

import java.io.IOException;

/**
 * Implementation of the LBMS system requirements
 *
 * @author  sja9291     Spencer Alderman
 */
public class LBMS {

    /** TODO: Remove in production */
    public static final boolean PREPRODUCTION_DEBUG = true;
    //The server we're running
    private final Server<TextMessage> server;

    //starts the server and initializes its storage
    private LBMS() {
        this.server = new TextServer();
        try {
            this.server.getLibraryData().initialize();
            this.server.getLibraryData().getLibrary().setClock(this.server.getClock());
            this.server.getBookStore().initialize();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Clock.shutdown();
                    this.server.getLibraryData().cleanup();
                    this.server.getBookStore().cleanup();
                } catch (IOException e) {
                    System.err.println("Fatal error while saving library data: ");
                    e.printStackTrace();
                }
            }, "LBMS-shutdown"));
        } catch (IOException e) {
            //because this is a fatal startup issue
            Errors.reportAndExit("Fatal error while starting LBMS storage", e);
        }
    }

    //start a client for us to access the server with, and display it
    private void access() {
        try (Client<TextMessage> client = new TextClient(System.in, System.out)) {
            client.connect(this.server);
            client.display();
        } catch (Exception e) {
            Errors.report(e);
        }
    }

    /**
     * Entrypoint for our program
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String... args) {
        Debugger.toggleOutput(false);
        LBMS system = new LBMS();
        system.access(); //will consume main thread
    }
}
