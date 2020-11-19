package edu.rit.codelanx;

import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.storage.Query;
import edu.rit.codelanx.network.client.Client;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.network.server.TextServer;
import edu.rit.codelanx.util.Clock;

import java.io.IOException;

/**
 * Implementation of the LBMS system requirements
 *
 * @author  sja9291     Spencer Alderman
 */
public class LBMS {

    /** TODO: Remove in production */
    public static final boolean PREPRODUCTION_DEBUG = true;

    /**
     * Entrypoint for our program
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String... args) { //just runs a server
        try {
            LBMS.startServer();
        } catch (Throwable e) { //print em all
            e.printStackTrace();
            try {
                //and we'll wait for a confirmation from the user
                System.out.println("Press any key to continue...");
                System.in.read();
            } catch (IOException ignored) {} //purpose served
        }
    }

    private static void registerOpenCloseTasks(Server<?> server) {
        Clock clock = server.getClock();
        int open = PREPRODUCTION_DEBUG ? 1 : ConfigKey.LIBRARY_OPEN_TIME.as(int.class);
        int close = PREPRODUCTION_DEBUG ? 86399 : ConfigKey.LIBRARY_CLOSE_TIME.as(int.class);
        Query<? extends Library> libs = server.getLibraryData().query(Library.class);
        clock.registerTask(open, () -> libs.results().forEach(Library::open)); //Free stress test!
        clock.registerTask(close, () -> libs.results().forEach(Library::close));
    }

    public static Server<TextMessage> startServer() throws IOException {
        Server<TextMessage> back = new TextServer();
        try {
            back.getLibraryData().initialize();
            back.getLibraryData().getLibrary().setClock(back.getClock());
            registerOpenCloseTasks(back);
            back.getBookStore().initialize();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    back.getLibraryData().cleanup();
                    back.getBookStore().cleanup();
                } catch (IOException e) {
                    System.err.println("Fatal error while saving library data: ");
                    e.printStackTrace();
                }
            }, "LBMS-shutdown"));
        } catch (IOException e) {
            //because this is a fatal startup issue
            throw new IOException("Fatal error while starting LBMS storage", e);
        }
        return back;
    }

    public static void connect(Client<TextMessage> client, Server<TextMessage> server) {

    }

    public static void run(Client<TextMessage> client, Server<TextMessage> server) throws IOException {
        try {
            client.connect(server);
            client.display();
        } catch (IOException e) {
            throw new IOException("Error displaying client", e);
        }
    }
}
