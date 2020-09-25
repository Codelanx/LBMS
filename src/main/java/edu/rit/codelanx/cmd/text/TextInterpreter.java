package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.TextCommandMap;
import edu.rit.codelanx.ui.Client;
import edu.rit.codelanx.ui.IMessage;
import edu.rit.codelanx.ui.TextClient;
import edu.rit.codelanx.ui.TextMessage;

public class TextInterpreter implements Interpreter<String,TextMessage> {

    private final Server server;

    public TextInterpreter(Server server) {
        this.server = server;
        TextCommandMap.initialize(server); //Enables commands on this server
    }

    /**
     * checks if request str is terminated or not.
     * @param request
     * @return
     */
    //TODO: Revise this method.
    public String terminatedRequest(TextRequest request){
        String content = request.getData();
        String r=null;
        int index;
        if ((index=content.indexOf(';'))!=-1){
            r=content.substring(0,index);
        }
        return r;
    }


    @Override
    public void receive(Client executor, TextMessage request) {
        //TODO: Handle receiving a request here
        //was it terminated? if so, exec a command
    }
}
