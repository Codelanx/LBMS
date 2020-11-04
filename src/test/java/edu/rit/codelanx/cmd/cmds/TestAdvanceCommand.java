package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.util.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opentest4j.AssertionFailedError;

import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Matchers.any;

public class TestAdvanceCommand {

    private final String VALID_HOURS = "7";
    private final String INVALID_HOURS = "24";
    private final String VALID_DAYS = "5";
    private final String INVALID_DAYS = "7";

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
    public void testNoInput() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: All inputs should be able to be handled, clock shouldn't change
         */
        assertSame(ResponseFlag.SUCCESS, adv.onExecute(execMock, "",""));
        assertSame(ResponseFlag.SUCCESS, adv.onExecute(execMock));
    }

    @Test
    public void wrongDays() {
        /*
        Test Explanation: Testing sending the wrong amount of days, the hours don't matter
        Expectation: All inputs should be able to be handled, clock should change if hours are valid
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, INVALID_DAYS, INVALID_HOURS));
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, INVALID_DAYS, VALID_HOURS));
    }

    @Test
    public void wrongHours() {
        /*
        Test Explanation: Testing sending the wrong amount of hours, the days don't matter
        Expectation: All inputs should be able to be handled, clock should change if days are valid
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, INVALID_HOURS));
    }

    @Test
    public void happyPathDays() {
        /*
        Test Explanation: Testing sending the correct amount of days, hours don't matter
        Expectation: All inputs should be able to be handled, clock should change
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, ""));
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, VALID_HOURS));
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, INVALID_HOURS));
    }

    @Test
    public void happyPathHours() {
        /*
        Test Explanation: Testing sending the correct amount of hours, days don't matter
        Expectation: All inputs should be able to be handled, clock should change
        */
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, "", VALID_HOURS));
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, VALID_DAYS, VALID_HOURS));
        assertSame(ResponseFlag.SUCCESS, this.adv.onExecute(this.execMock, INVALID_DAYS, VALID_HOURS));
    }
}
