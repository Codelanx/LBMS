package edu.rit.codelanx;

import edu.rit.codelanx.ui.Client;
import edu.rit.codelanx.ui.TextClient;
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
        this.server = new LibServer();
        try (TextClient client = new TextClient(System.in, System.out)) {
            client.sendMessage("Hello world!");
            client.display();
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
        LBMS system = new LBMS(); //will consume main thread
    }
}
