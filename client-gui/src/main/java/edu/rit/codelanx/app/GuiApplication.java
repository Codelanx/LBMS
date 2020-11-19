package edu.rit.codelanx.app;

import edu.rit.codelanx.LBMS;
import edu.rit.codelanx.network.client.gui.GuiClient;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import javafx.application.Application;
import javafx.stage.Stage;

public class GuiApplication extends Application {

    private volatile Server<TextMessage> server;

    @Override
    public void init() throws Exception {
        this.server = LBMS.startServer();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LBMS.run(new GuiClient(primaryStage), this.server);
    }

}
