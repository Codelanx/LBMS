package edu.rit.codelanx;

import edu.rit.codelanx.network.client.Client;
import edu.rit.codelanx.network.client.TextClient;
import edu.rit.codelanx.network.server.TextServer;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.util.Errors;

import java.io.IOException;

/**
 * Implementation of the LBMS system requirements
 *
 * @author  sja9291     Spencer Alderman
 */
public class LBMS {

    private final Server server;

    private LBMS() {
        this.server = new TextServer();
        try {
            this.server.getDataStorage().initialize();
        } catch (IOException e) {
            System.err.println("Fatal error while starting LBMS storage");
            Errors.report(e);
            throw new Error(e); //Error, because this is a fatal startup issue
        }
    }

    private void access() {
        try (Client client = new TextClient(System.in, System.out)) {
            client.connect(this.server);
            client.display();
            client.sendMessage("Hello world!");
        } catch (Exception e) {
            Errors.report(e);
        }
    }

    /**
     * Entrypoint for our program
     *
     * @param args command-line arguments
     */
    public static void main(String... args) {
        LBMS system = new LBMS();
        system.access(); //will consume main thread
    }
}
