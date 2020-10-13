package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class TestAdvanceCommand {
    private @Mock
    Server<TextMessage> servMock;
    private @Mock
    CommandExecutor execMock;

    @Test
    public void testNoInput() {
        // TODO: Call advance;
        // Should send advance,missing-params,numberofdays,numberofhours; and ResponseFlag.Success;
        // Works
    }

    @Test
    public void tooMuchInput() {
        // TODO: Call advance and try to pass in 3 inputs
        // Should send advance,success; and ResponseFlag.Success;
        // Works
    }

    @Test
    public void wrongDays() {
        // TODO: Call advance with too many days
        // Should send advance,invalid-number-of-days,numberOfDays;
        // and ResponseFlag.Success
        // Works
    }

    @Test
    public void wrongHours() {
        // TODO: Call advance with too many hours
        // Should send advance,invalid-number-of-hours,numberOfDays; and ResponseFlag.Success
        // Works
    }

    @Test
    public void happyPathDays() {
        // TODO: Call datetime, call arrive,numberofdays;, check both to see
        //  if they make sense
        // Should send arrive,success; and ResponseFlag.Success
        // Works
    }

    @Test
    public void happyPathDaysHours(){
        // TODO: Call datetime, call arrive,numberofdays = 0,numberofhours;,
        //  check both to see if they make sense
        // Works
    }
}
