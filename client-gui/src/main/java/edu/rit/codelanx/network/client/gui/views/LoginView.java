package edu.rit.codelanx.network.client.gui.views;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.network.client.gui.ClientNode;
import edu.rit.codelanx.network.client.gui.GuiClient;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.Optional;

public class LoginView extends ClientNode {

    private final GridPane pane;

    public LoginView(GuiClient client) {
        super(client);
        this.pane = new GridPane();
        this.pane.setAlignment(Pos.CENTER);
        this.pane.setHgap(10);
        this.pane.setVgap(10);
        this.pane.setPadding(new Insets(25, 25, 25, 25));

        Text title = new Text("Welcome");

        Button reg = new Button("Register");
        reg.addEventHandler(ActionEvent.ACTION, event -> {
            client.setRoot(new RegisterView(client, this));
        });

        Text error = new Text();
        error.getStyleClass().add("error");
        Text label = new Text("Visitor ID:");
        TextField field = new TextField();

        Button login = new Button("Login");
        login.addEventHandler(ActionEvent.ACTION, event -> {
            CharSequence val = field.getCharacters();
            if (val.length() == 0) {
                field.getStyleClass().add("error");
                error.setText("Visitor ID Required");
            }
            String resp = client.sendIfConnected("arrive," + val + ";");
            if (resp == null) {
                error.setText("Error: Not connected to server");
                return;
            }
            switch (resp) {
                case "arrive,invalid-id;":
                    error.setText("Error: Invalid visitor ID");
                    break;
                case "arrive,duplicate;":
                    error.setText("Error: Visitor already logged in");
                    break;
                case "arrive,library-is-closed;":
                    error.setText("Error: Library closed");
                    break;
                default:
                    String[] tokens = resp.split(TextCommand.TOKEN_DELIMITER);
                    Optional<Long> id = tokens.length > 2 && !tokens[1].equalsIgnoreCase("error")
                            ? InputOutput.parseLong(tokens[1])
                            : Optional.empty();
                    if (id.isPresent()) {
                        client.setLoginID(id.get());
                        client.setRoot(new PrimaryView(client));
                    } else {
                        error.setText("Error: " + resp);
                    }
                    break;
            }
        });
        this.pane.add(title, 0, 0);
        this.pane.add(label, 0, 1);
        this.pane.add(field, 1, 1, 2, 1);
        this.pane.add(error, 1, 2, 2, 1);
        this.pane.add(reg, 1, 3);
        this.pane.add(login, 2, 3);
    }

    @Override
    public Parent getRoot() {
        return this.pane;
    }
}
