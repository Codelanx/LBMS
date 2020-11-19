package edu.rit.codelanx.app;

import edu.rit.codelanx.LBMS;
import edu.rit.codelanx.network.client.text.TextClient;

import java.io.IOException;

public class TextApplication extends LBMS {

    public static void main(String... args) {
        try {
            LBMS.run(new TextClient(System.in, System.out), LBMS.startServer());
        } catch (IOException e) {
            e.printStackTrace(); //fatal error starting up
            try {
                System.out.println("Press any key to continue...");
                System.in.read(); //prompt for input
            } catch (IOException ignored) {} //purpose served
        }
    }
}
