package edu.rit.codelanx;

import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.ui.Client;
import edu.rit.codelanx.ui.IMessage;
import edu.rit.codelanx.ui.TextMessage;

public interface Server {

    public DataStorage getDataStorage();
    public void receive(Client client, TextMessage message);
    public Interpreter<String, TextMessage> getInterpreter();
    public void registerClient(Client client);
}
