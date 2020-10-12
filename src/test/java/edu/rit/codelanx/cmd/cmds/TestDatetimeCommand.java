package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class TestDatetimeCommand {

    private @Mock
    Server<TextMessage> servMock;
    private @Mock
    CommandExecutor execMock;

    @Test
    public void tooMuchInput() {
        // TODO: Call datetime,anyArgument;
        // Should send ResponseFlag.Success
        // Works
    }

    @Test
    public void happyPathDays() {
        // TODO: Call datetime;
        // Should send datetime,YYYY/MM/DD,HH:MM:SS; and ResponseFlag.Success
        // Works
    }
}
