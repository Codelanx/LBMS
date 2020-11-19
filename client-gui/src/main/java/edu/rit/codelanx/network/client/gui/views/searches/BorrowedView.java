package edu.rit.codelanx.network.client.gui.views.searches;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.client.gui.ClientNode;
import edu.rit.codelanx.network.client.gui.GuiClient;
import edu.rit.codelanx.network.client.gui.views.SearchView;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Optional;
import java.util.stream.Collectors;


//Whew, this is some ugly code below. About as DRY as the titanic
public class BorrowedView extends SearchView<Book> {

    private static final String[] INPUTS = {"Visitor ID", "Authors, ...", "ISBN", "Publisher", "Sort Order"};
    private static final DataField<?>[] FIELDS = {Visitor.Field.ID, Book.Field.ID};
    private static final String[] RVALS = {"Available copies", "ID", "ISBN", "Title", "Authors", "Publisher", "Publish Date", "Total Copies"};

    public BorrowedView(GuiClient client, ClientNode previous) { //TODO: Just show their current listing, duh!
        super(client, previous);
        String resp = client.sendIfConnected("borrowed," + client.getLoginID() + ";");
        if (resp == null || resp.equalsIgnoreCase("borrow,invalid-visitor-id")) {
            //TODO: Error
        }
        //TODO: This is a suspect area for an off-by-one area, this code was quick
        int c = resp.indexOf('\n');
        if (c < 0) {
            //No argS?
            if (resp.length() <= 0 || resp.charAt(0) == 0) {
                return; //TODO: Error?
            }
        }
        Optional<Integer> opt = InputOutput.parseInt(c < 0 ? resp : resp.substring(0, c));
        if (!opt.isPresent()) {
            return; //TODO: Error
        }
        int n = opt.get();
        if (n == 0) return; //Nothing to do
        String[] rem = resp.split("\n");
        for (int i = 0; i < n; i++) {
            String[] vals = rem[i].split(TextCommand.TOKEN_DELIMITER);
            //this.ap //TODO: Out of time
        }
        /*.ifPresent(n -> {
            if (n == 0) return;
            for (int i = 0; i < n; i++) {
                //read each line
                String[] args = ((resp = resp.substring(0, (c = resp.indexOf(TextCommand.TOKEN_DELIMITER)))));
            }
        });*/

    }

    @Override
    protected String getCommand() {
        return "borrowed";
    }

    @Override
    protected String[] getFieldNames() {
        return INPUTS;
    }

    @Override
    protected String[] getReturnParams() {
        return RVALS;
    }

    @Override
    protected TableColumn<Book, String> apply(TableView<Book> table, int index) {
        return new TableColumn<>("Dummy Column"); //TODO:
    }

    @Override
    protected TableView<Book> getTableView() {
        TableView<Book> back = super.getTableView();
        //TODO: Map all of these to actual states and put them in the table
        //this is a total hackaround...
        back.setItems(this.client.__UNSAFEgetServer().getLibraryData().query(Book.class)
                //.filterBy(Book.Field.ISBN, val -> )
                .isAny(Book.Field.ISBN) // TODO: Get ids
                .results()
                .collect(Collectors.toCollection(SimpleListProperty::new)));
        return back;
    }
}
