package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class TestArriveCommand {

    private @Mock
    Server<TextMessage> servMock;
    private @Mock
    CommandExecutor execMock;

    @Test
    public void testNoInput() {
        // TODO: Call arrive;
        // Should send arrive,missing-params,visitor-id; and ResponseFlag.Success;
        // Works
    }

    @Test
    public void tooMuchInput() {
        // TODO: Register new visitor, try to pass in 2 or more args to arrive
        // Should send arrive,invalid-id; and ResponseFlag.Success
        // Works
    }

    @Test
    public void wrongIDType() {
        // TODO: Register new visitor, try to pass in a letter as visitorID
        // Should send ResponseFlag.Failure
        // Works
    }

    @Test
    public void duplicateVisitStart() {
        // TODO: Register new visitor, call arrive,visitorID; twice
        // Should send arrive,duplicate; and Responseflag.Success
        // TODO: Doesn't Work
    }

    @Test
    public void happyPath() {
        //TODO: Register new visitor, call arrive,visitorID;
        //Should send arrive,visitorID,time; and ResponseFlag.Success
        //Works
    }
}
