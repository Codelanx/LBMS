package edu.rit.codelanx.network.client.gui.views;

import edu.rit.codelanx.network.client.gui.ClientNode;
import edu.rit.codelanx.network.client.gui.GuiClient;
import edu.rit.codelanx.network.client.gui.views.searches.BookStoreView;
import edu.rit.codelanx.network.client.gui.views.searches.BookView;
import edu.rit.codelanx.network.client.gui.views.searches.BorrowedView;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

public class PrimaryView extends ClientNode {

    private final GridPane pane;

    public PrimaryView(GuiClient client) {
        super(client);
        this.pane = new GridPane();
        this.pane.setAlignment(Pos.CENTER);
        this.pane.setHgap(10);
        this.pane.setVgap(10);
        this.pane.setPadding(new Insets(25, 25, 25, 25));

        Button bookStore = new Button("Book Store");
        Button libBooks = new Button("Borrow a Book");
        Button borrowed = new Button("View borrowed books");
        Dialog<?> d = new Dialog<>();
        d.setOnCloseRequest(event -> {
            d.close();
        });
        bookStore.addEventHandler(ActionEvent.ACTION, event -> {
            d.getDialogPane().setContent(new BookStoreView(client, this).getRoot());
            d.show();
        });
        libBooks.addEventHandler(ActionEvent.ACTION, event -> {
            d.getDialogPane().setContent(new BookView(client, this).getRoot());
            d.show();
        });
        borrowed.addEventHandler(ActionEvent.ACTION, event -> {
            d.getDialogPane().setContent(new BorrowedView(client, this).getRoot());
            d.show();
        });
        this.pane.add(bookStore, 0, 0);
        this.pane.add(libBooks, 0, 1);
        this.pane.add(borrowed, 0, 2);
    }

    @Override
    public Parent getRoot() {
        return this.pane;
    }
}
