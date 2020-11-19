package edu.rit.codelanx.network.client.gui.views;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.client.gui.ClientNode;
import edu.rit.codelanx.network.client.gui.GuiClient;
import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

public abstract class SearchView<T> extends ClientNode {

    private final GridPane pane;
    private final TextField[] values;
    private final StringBuilder output;

    public SearchView(GuiClient client, ClientNode previous) {
        super(client, previous);
        String[] fields = this.getFieldNames();
        String[] rvals = this.getReturnParams();
        if (fields.length > 5) {
            //we will intentionally -not- validate the user's input on these fields
            // this would be a practical design improvement further down the line,
            // but for now it simply assumes "innocent" input (e.g. no commas where
            // they shouldn't be)
            throw new IllegalArgumentException("Cannot format more than 5 search parameters");
        }
        this.values = new TextField[fields.length];
        this.output = new StringBuilder(this.getCommand());
        this.pane = new GridPane();
        for (int i = 0; i < fields.length; i++) {
            TextField field = this.values[i] = new TextField();
            field.setPromptText(fields[i]);
            this.pane.add(field, i, 0);
        }
        //TODO: Giant tableview or something
        Text error = new Text();
        error.getStyleClass().add("error");
        Button confirm = new Button("Search");
        confirm.setDefaultButton(true);
        confirm.addEventHandler(ActionEvent.ACTION,  event -> {
            error.setText("");
            for (int i = 0; i < fields.length; i++) {
                CharSequence s = this.values[i].getCharacters();
                this.output.append(TextCommand.TOKEN_DELIMITER);
                if (s.length() == 0) continue;
                this.output.append(s);
            }
            String out = this.output.toString();
            this.output.setLength(this.getCommand().length());
            String resp = this.client.sendIfConnected(out);
            if (resp == null) {
                error.setText("Error: Not connected to server");
                return;
            }
            int c = resp.indexOf(',');
            if (c < 0) {
                //no comma
                error.setText("Malformed response: " + resp);
                return;
            }
            resp = resp.substring(c);
            c = resp.indexOf(',');
            InputOutput.parseInt(c < 0 ? resp : resp.substring(0, c)).ifPresent(n -> {
                for (int i = 0; i < n; i++) {
                    //TODO: I don't even remember what I was writing at this point
                }
            });
            switch (resp) {
                case "invalid-sort-order":
                    break;
                case "invalid-visitor-id":
                    //well, it must be BorrowedCommand's search
                    break;
                default:
                    //TODO: ????
                    break;
            }
        });
        this.pane.add(this.getTableView(), 0, 1, 5, 3);
        this.pane.add(error, 2, 6, 2, 1);
        this.pane.add(confirm, 4, 6);
    }

    @Override
    public Parent getRoot() {
        return this.pane;
    }

    protected abstract String getCommand();
    protected abstract String[] getFieldNames();
    protected abstract String[] getReturnParams();
    protected TableView<T> getTableView() {
        TableView<T> table = new TableView<>();
        table.setEditable(false);
        table.setMinHeight(300);
        int end = this.getReturnParams().length;
        for (int i = 0; i < end; i++) {
            table.getColumns().add(this.apply(table, i));
        }
        return table;
    }
    protected abstract TableColumn<T, String> apply(TableView<T> table, int index);


    /*
     * BorrowCommand - button
     * BuyCommand - button
     * ReturnCommand - button
     * BorrowedCommand - scene          TODO: BookView
     * InfoCommand - scene              TODO: BookView
     * SearchCommand - scene            TODO: BookView
     * DatetimeCommand - static control
     * PayCommand - prompt/input/button
     */
}
