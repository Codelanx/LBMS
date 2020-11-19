package edu.rit.codelanx.network.client.gui.views.searches;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.client.gui.ClientNode;
import edu.rit.codelanx.network.client.gui.GuiClient;
import edu.rit.codelanx.network.client.gui.views.SearchView;
import edu.rit.codelanx.util.Errors;
import javafx.beans.property.StringPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.rit.codelanx.data.state.types.Book.Field.*;

public class BookView extends SearchView<Book> {

    private static final String[] INPUTS = {"Title", "Authors, ...", "ISBN", "Publisher", "Sort Order"};
    private static final DataField<?>[] FIELDS = {null, ID, ISBN, TITLE, null, PUBLISHER, PUBLISH_DATE, TOTAL_COPIES};
    private static final String[] RVALS = {"Available copies", "ID", "ISBN", "Title", "Authors", "Publisher", "Publish Date", "Total Copies"};

    public BookView(GuiClient client, ClientNode previous) {
        super(client, previous);
    }

    @Override
    protected String getCommand() {
        return "info";
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
    protected TableColumn<Book, String> apply(TableView<Book> table, int i) {
        TableColumn<Book, String> obj = new TableColumn<>(RVALS[i]);
        Function<Book, Object> mapper = null;
        if (FIELDS[i] == null) {
            //need to handle special cases
            if (i == 0) {
                mapper = book -> book.getAvailableCopies() + "";
            }
            if (i == 4) { //Authors, ...
                mapper = book -> book.getAuthors()
                        .map(Author::getName)
                        .collect(Collectors.joining(", "));
            }
        } else {
            mapper = book -> Objects.toString(FIELDS[i].get(book));
        }
        if (mapper == null) {
            Errors.reportAndExit(new IllegalArgumentException("Unsupported column mapping in SearchView"));
            return null;
        }
        final Function<Book, Object> fmap = mapper; //final just to show off why it's here
        obj.setCellValueFactory(book ->
                new StringPropertyBase() {
                    @Override
                    public Object getBean() {
                        return fmap.apply(book.getValue());
                    }

                    @Override
                    public String getName() {
                        return RVALS[i];
                    }
                });
        return obj;
    }
}
