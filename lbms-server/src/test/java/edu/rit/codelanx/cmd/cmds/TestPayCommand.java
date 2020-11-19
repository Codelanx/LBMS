package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class TestPayCommand {
    private final String VALID_VISITOR_ID = "7";
    private final String INVALID_VISITOR_ID = "2";
    private final String VALID_AMOUNT="15.7";
    private final String UNDER_AMOUNT="-1.0";
    private final String OVER_AMOUNT="17.5";
    private final String BALANCE="-15.7";
    private long id = 7;
    private long invalid_id = 2;
    private BigDecimal valid_amount= new BigDecimal(VALID_AMOUNT);
    private BigDecimal balance= new BigDecimal(BALANCE);


    private PayCommand cmd;
    private PayCommand cmd_spy;

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;
    @Mock
    private DataSource dataSourceMock;
    @Mock
    private Library libraryMock;
    @Mock
    private Visitor visitorMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cmd = new PayCommand(this.servMock);
        cmd_spy = Mockito.spy(cmd);

        Mockito.when(servMock.getLibraryData()).thenReturn(dataSourceMock);
        Mockito.when(dataSourceMock.getLibrary()).thenReturn(libraryMock);
        Mockito.when(libraryMock.isOpen()).thenReturn(true);

        Mockito.doReturn(visitorMock).when(cmd_spy).getVisitor(id);
        Mockito.doReturn(null).when(cmd_spy).getVisitor(not(eq(id)));
        Mockito.doReturn("").when(cmd_spy).performPayTransaction(any(), any());
        Mockito.when(visitorMock.getID()).thenReturn(id);
        Mockito.when(visitorMock.isVisiting()).thenReturn(true);
        Mockito.when(visitorMock.getMoney()).thenReturn(balance);
    }

    @Test
    public void TestInvalidVisitorID() {
         /*
        Test Explanation: send invalid Visitor ID to pay command
        Expectation: return SUCCESS response flag, but not actually paying
         */
        Assertions.assertEquals(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, INVALID_VISITOR_ID, VALID_AMOUNT));
        verify(cmd_spy, times(1)).getVisitor(invalid_id);
        verify(cmd_spy, never()).performPayTransaction(any(), any());
    }
    @Test
    public void TestHappyPath() {
        /*
        Test Explanation: send a valid ID and amount to pay command
        Expectation: return SUCCESS response flag, and peform paying transaction
         */
        Assertions.assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, VALID_VISITOR_ID, VALID_AMOUNT));
        verify(cmd_spy, times(1)).getVisitor(id);
        verify(cmd_spy, times(1)).performPayTransaction(any(),any());

    }
    @Test
    public void PayAnInvalidAmount() {
        /*
        Test Explanation: send invalid amount to pay command
        Expectation: return SUCCESS response flag, but not actually paying
         */
        Assertions.assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, VALID_VISITOR_ID, UNDER_AMOUNT));
        Assertions.assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, VALID_VISITOR_ID, OVER_AMOUNT));
        verify(cmd_spy, times(2)).getVisitor(id);
        verify(cmd_spy, never()).performPayTransaction(any(),any());
    }

}
