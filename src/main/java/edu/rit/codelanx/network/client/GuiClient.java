package edu.rit.codelanx.network.client;

import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.data.state.State;

import java.io.IOException;

//dummy class for now, but just to verify our sanity
public class GuiClient implements Client<TextMessage> {

    @Override
    public void connect(Server<TextMessage> server) {

    }

    @Override
    public void display() throws IOException {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void renderState(State... state) {

    }

    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {

    }

    @Override
    public void close() throws Exception {

    }
}
