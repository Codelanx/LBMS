package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.util.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Matchers.any;

public class TestAdvanceCommand {

    private final String VALID_HOURS = "7";
    private final String INVALID_HOURS = "24";
    private final String VALID_DAYS = "5";
    private final String INVALID_DAYS = "8";

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;
    @Mock
    private Clock clockMock;

    private AdvanceCommand adv;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        clockMock = Mockito.mock(Clock.class);
        Mockito.when(servMock.getClock()).thenReturn(clockMock);
        this.adv = new AdvanceCommand(this.servMock);
    }


    @Test
    public void wrongDays() {
        /*
        Test Explanation: Testing sending the wrong amount of days, the hours don't matter
        Expectation: All inputs should be able to be handled, clock should change if hours are valid
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, INVALID_DAYS, INVALID_HOURS));
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, INVALID_DAYS, VALID_HOURS));
        Mockito.verify(execMock, Mockito.times(2)).sendMessage("advance,invalid-number-of-days," + INVALID_DAYS + ";");
    }

    @Test
    public void wrongHours() {
        /*
        Test Explanation: Testing sending the wrong amount of hours, the days don't matter
        Expectation: All inputs should be able to be handled, clock should change if days are valid
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, INVALID_HOURS));
        Mockito.verify(execMock).sendMessage("advance,invalid-number-of-hours," + INVALID_HOURS + ";");
    }

    @Test
    public void happyPathDays() {
        /*
        Test Explanation: Testing sending the correct amount of days
        Expectation: All inputs should be able to be handled, clock should change
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, ""));
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, VALID_HOURS));
        Mockito.verify(execMock, Mockito.times(2)).sendMessage("advance,success;");
    }

    @Test
    public void happyPathHours() {
        /*
        Test Explanation: Testing sending the correct amount of hours
        Expectation: All inputs should be able to be handled, clock should change
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, VALID_HOURS));
        Mockito.verify(execMock).sendMessage("advance,success;");
    }
}
