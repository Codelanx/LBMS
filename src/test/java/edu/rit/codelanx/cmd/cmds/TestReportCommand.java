package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.state.types.Transaction;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.util.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

public class TestReportCommand {
    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;
    @Mock
    private Clock clockMock;

    private ReportCommand repSpy;
    private ReportCommand rep;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        clockMock = Mockito.mock(Clock.class);

        this.rep = new ReportCommand(this.servMock);
        repSpy = Mockito.spy(rep);

        Mockito.when(servMock.getClock()).thenReturn(clockMock);
        Mockito.doReturn(Instant.now()).when(clockMock).getCurrentTime();
        Mockito.doReturn(3L).when(repSpy).getBookCount();
        Mockito.doReturn(2L).when(repSpy).getNewVisitorCount();
        Mockito.doReturn(1L).when(repSpy).getBooksPurchasedAmount();
        Mockito.doReturn(3600.00).when(repSpy).getAverageVisitLength();
        Mockito.doReturn(Collections.<String, Set<Transaction>>emptyMap()).when(repSpy).getTransactions();
    }

    @Test
    public void incorrectDays(){
        /*
        Test Explanation: Testing report when passed a non-number amount of days
        Expectation: All inputs should be able to be handled, no report should be generated
         */
        Assertions.assertEquals(ResponseFlag.SUCCESS, repSpy.onExecute(this.execMock, "A"));
        Mockito.verify(execMock).sendMessage("report,invalid-argument");
    }

    @Test
    public void happyPath(){
        /*
        Test Explanation: Testing report when called correctly
        Expectation: All inputs should be able to be handled, reports should be generated
         */
        Assertions.assertEquals(ResponseFlag.SUCCESS, repSpy.onExecute(this.execMock, "1"));
        Mockito.verify(execMock).sendMessage("report"
                + "Date Generated: " + Matchers.any()
                + "\n Number of Books: " + "3"
                + "\n Number of Visitors: " + "2"
                + "\n Average Length of Visit: " + "3600"
                + "\n Number of Books Purchased: " + "1"
                + "\n Fines Collected: " + "0"
                + "\n Fines Outstanding: " + 0);
        Mockito.verify(repSpy, Mockito.times(1)).getBookCount();
        Mockito.verify(repSpy, Mockito.times(1)).getNewVisitorCount();
        Mockito.verify(repSpy, Mockito.times(1)).getAverageVisitLength();
        Mockito.verify(repSpy, Mockito.times(1)).getBooksPurchasedAmount();
    }
}
