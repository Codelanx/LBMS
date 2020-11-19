package edu.rit.codelanx.network.client.gui;

import javafx.scene.Parent;

public abstract class ClientNode {

    protected final GuiClient client;
    protected final ClientNode previous;

    public ClientNode(GuiClient client) {
        this(client, null);
    }

    public ClientNode(GuiClient client, ClientNode previous) {
        this.client = client;
        this.previous = previous;
    }

    public abstract Parent getRoot();

}
