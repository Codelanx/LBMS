package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.TextCommandMap;
import edu.rit.codelanx.ui.Client;

public class TextInterpreter implements Interpreter<TextRequest, TextResponse> {

    private final Server server;

    public TextInterpreter(Server server) {
        this.server = server;
        TextCommandMap.initialize(server); //Enables commands on this server
    }

    @Override
    public TextResponse receive(Client executor, TextRequest request) {
        //TODO: Handle receiving a request here
        //was it terminated? if so, exec a command
        return null;
    }


    /**
     * checks if request str is terminated otr not.
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
}
