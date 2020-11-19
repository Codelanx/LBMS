package edu.rit.codelanx.network.client.gui;

import com.codelanx.commons.util.Parallel;
import edu.rit.codelanx.network.client.Client;
import edu.rit.codelanx.network.client.gui.menu.FileMenu;
import edu.rit.codelanx.network.client.gui.views.LoginView;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GuiClient implements Client<TextMessage> {

    public static final long LOGOUT_ID = 0L;
    static final int DEFAULT_HEIGHT = 480;
    static final int DEFAULT_WIDTH = 720;
    private final AtomicReference<Server<TextMessage>> server = new AtomicReference<>();
    private final ClientNode landing = new LoginView(this);
    private final Deque<String> msgQueue = new ArrayDeque<>();
    private final Lock commandLock = new ReentrantLock();
    private volatile Stage display;
    private volatile BorderPane primary;
    private volatile StackPane root;

    private final LongProperty loginID = new SimpleLongProperty(this, "login-id", LOGOUT_ID);

    public GuiClient(Stage stage) {
        this.display = stage;
        this.initialize();
    }

    @Deprecated //Are you discouraged yet?
    public Server<TextMessage> __UNSAFEgetServer() {
        return this.server.get();
    }

    public void setLoginID(long id) {
        this.loginID.set(id);
    }

    public LongProperty loginIDProperty() {
        return this.loginID;
    }

    public long getLoginID() {
        return this.loginID.get();
    }

    public boolean isLoggedIn() {
        return this.loginID.get() > 0;
    }

    public void setRoot(ClientNode node) {
        this.primary.setCenter(node.getRoot());
    }

    private void initialize() {
        this.display.setTitle("Library Book Management System v0.0.1");
        //this.display.setAlwaysOnTop(true);
        this.root = new StackPane();
        this.primary = new BorderPane();
        this.root.getChildren().add(this.primary);

        MenuBar bar = new MenuBar(new FileMenu(this));
        this.primary.setTop(bar);

        this.setRoot(new LoginView(this));

        /* TODO: Gui per command
         *
         * AdvanceCommand - menu opt
         * ArriveCommand - main pane?       TODO: StartView, LoginView
         * BorrowCommand - button
         * BorrowedCommand - scene          TODO: BookView
         * BuyCommand - button
         * DatetimeCommand - static control?
         * DepartCommand - main pane exit?
         * InfoCommand - scene              TODO: BookView
         * PayCommand - prompt/input/button
         * RegisterCommand - main pane?     TODO: StartView, RegView
         * ReportCommand - menu opt
         * ReturnCommand - button
         * SearchCommand - scene            TODO: BookView
         */
        /*
         * Land on main page:
         *
         *  < New Visitor , Returning Visitor >
         *
         * New Visitor:
         *  - prompt page for first/last/addr/phone, on continue exec register
         *
         *  Returning Visitor:
         *  - "login" prompt for visitor id, exec arrive
         *
         *  TODO: Main view
         *
         *      < Search Books, Manage checkouts, Find new books >
         *
         * All options lead to submenus involving a querying for books
         *  (checkouts being all of the current relevant ones)
         */
        //ClientFactory.notifyGUIClient(this); //Releases the lock on the LBMS client factory, signalling we're ready

        this.display.setScene(new Scene(this.root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    @Override
    public void connect(Server<TextMessage> server) {
        this.server.set(server);
    }

    @Override
    public void display() {
        this.display.show();
    }

    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {
        /*Alert resp = new Alert(Alert.AlertType.INFORMATION, message.getData());
        resp.show();*/
        this.msgQueue.add(message.getData());
        //TODO: Display as pop-up (prompt?)
    }

    public boolean isConnected() {
        return this.server.get() != null;
    }

    public String sendIfConnected(String message) {
        Server<TextMessage> serv = this.server.get();
        if (serv == null) return null;
        String back = Parallel.operateLock(this.commandLock, () -> {
            this.msgQueue.clear();
            this.sendTo(serv, new TextMessage(message));
            //because of the single-threaded nature of this, we can
            //simply return whatever we got through #receive from this call
            return String.join("\n", this.msgQueue);
        });
        this.msgQueue.clear();
        return back;
    }

    @Override
    public void close() throws Exception {
        //this.display.close();
    }

    public void showBanner(String message) {
        this.showBanner(message, Color.CORNFLOWERBLUE);
    }

    public void showBanner(String message, Color color) {
        HBox box = new HBox();
        Text text = new Text(message);
        text.getStyleClass().add("banner");
        box.getChildren().add(text);
        box.getStyleClass().add("banner");
        box.setStyle("-fx-background-color: #" + this.colorToHex(color));
        this.primary.setTop(box);
    }

    private String colorToHex(Color color) {
        //shift out the alpha bits
        return Integer.toString(color.hashCode() >> 8, 16);
    }

}
