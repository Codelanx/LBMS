package edu.rit.codelanx.network.client.gui.views;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.network.client.gui.ClientNode;
import edu.rit.codelanx.network.client.gui.GuiClient;
import edu.rit.codelanx.network.client.gui.LBMSColor;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

public class RegisterView extends ClientNode {

    private static final String[] FIELDS = {"First Name", "Last Name", "Address", "Phone Number"};
    private final GridPane pane;
    private final TextField[] fields = new TextField[FIELDS.length];
    private final Text[] errors = new Text[FIELDS.length];

    /*

        Register                            X

        First:              _________________
                             *required
        Last:               _________________

        Address:            _________________

        Phone number:       _________________

                                    [Confirm]

     */
    public RegisterView(GuiClient client, ClientNode caller) {
        super(client, caller);
        this.pane = new GridPane();
        this.pane.setAlignment(Pos.CENTER);
        this.pane.setHgap(10);
        this.pane.setVgap(10);
        this.pane.setPadding(new Insets(25, 25, 25, 25));

        Text title = new Text("Register");
        this.pane.add(title, 0, 0);
        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);
        cancel.addEventHandler(ActionEvent.ACTION, event -> {
            client.setRoot(caller);
        });

        for (int i = 0, row = 1; i < FIELDS.length; i++, row += 2) {
            this.pane.add(new Text(FIELDS[i] + ":"), 0, row);
            Text error = this.errors[i] = new Text();
            error.getStyleClass().add("error");
            TextField field = this.fields[i] = new TextField();
            field.addEventHandler(ActionEvent.ACTION, event -> {
                field.getStyleClass().remove("error");
                error.setText("");
            });
            this.pane.add(field, 1, row, 2, 1);
            this.pane.add(error, 1, row + 1, 2, 1);
        }

        Text genericError = new Text();
        genericError.getStyleClass().add("error");
        Button reg = new Button("Confirm");
        reg.addEventHandler(ActionEvent.ACTION, event -> {
            if (!client.isConnected()) {
                genericError.setText("Error: Not connected to server");
                return;
            }
            StringBuilder req = new StringBuilder("register");
            for (int i = 0; i < FIELDS.length; i++) {
                CharSequence val = this.fields[i].getCharacters();
                if (val.length() == 0) {
                    this.fields[i].getStyleClass().add("error");
                    this.errors[i].setText("Field required");
                    return;
                }
                req.append(',').append(val);
            }
            String resp = client.sendIfConnected(req.toString() + ';');
            if (resp == null) {
                genericError.setText("Error: Not connected to server");
                return;
            }
            switch (resp) {
                case "register,duplicate;":
                    genericError.setText("Error: Visitor already registered");
                    break;
                default:
                    String[] tokens = resp.split(TextCommand.TOKEN_DELIMITER);
                    Optional<Long> opt;
                    if (tokens.length > 2 && !tokens[1].equalsIgnoreCase("error")) {
                        opt = InputOutput.parseLong(tokens[1]);
                    } else {
                        opt = Optional.empty();
                    }
                    if (opt.isPresent()) {
                        client.setRoot(caller);
                        client.showBanner("Registered new visitor with ID: " + opt.get(), LBMSColor.SUCCESS);
                    } else {
                        genericError.setText("Error: " + resp);
                    }
                    break;
                    //error
            }
        });
        reg.setDefaultButton(true);
        int row = (FIELDS.length << 1) + 1;
        this.pane.add(genericError, 0, row, 2, 1);
        this.pane.add(cancel, 1, row);
        this.pane.add(reg, 2, row);
    }

    @Override
    public Parent getRoot() {
        return this.pane;
    }
}
