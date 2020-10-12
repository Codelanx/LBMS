package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class TestArriveCommand {

    private @Mock Server<TextMessage> serv;
    private @Mock CommandExecutor exec;

    @Test
    public void testNoInput(){
        ArriveCommand cmd = new ArriveCommand(this.serv);
        Assertions.assertSame(ResponseFlag.MISSING_ARGS,
                cmd.onExecute(this.exec));
        Mockito.verify(this.exec).sendMessage("arrive,missing-parameters," +
                "visitorID");
    }

    @Test
    public void happyPath(){
        String first = "Mark", last = "Anderson", address = "99 Route 66",
                phone = "518-867-5309";
        RegisterCommand cmd = new RegisterCommand(this.serv);
        cmd.onExecute(this.exec, first, last, address, phone);
        ArriveCommand arrcmd = new ArriveCommand(this.serv);
        arrcmd.onExecute(this.exec, "1");

    }
}
