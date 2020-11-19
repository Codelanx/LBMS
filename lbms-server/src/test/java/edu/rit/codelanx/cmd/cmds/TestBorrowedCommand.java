package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.types.Checkout;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class TestBorrowedCommand {
    private final String VALID_VISITOR_ID = "7";
    private long ID = 7;
    private final String INVALID_VISITOR_ID = "2";
    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;
    @Mock
    private DataSource DataSourceMock;
    @Mock
    private Library libMock;
    @Mock
    private Visitor visitorMock;
    @Mock
    private Checkout checkoutMock;

    private List<Checkout> borrowedList;
    private BorrowedCommand cmd;
    private BorrowedCommand cmd_spy;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.cmd = new BorrowedCommand(servMock);
        this.cmd_spy = Mockito.spy(cmd);
        //not checkout list empty
        borrowedList = new ArrayList<>();

        Mockito.when(servMock.getLibraryData()).thenReturn(DataSourceMock);
        Mockito.when(DataSourceMock.getLibrary()).thenReturn(libMock);
        Mockito.when(libMock.isOpen()).thenReturn(true);
        Mockito.doReturn(visitorMock).when(cmd_spy).getVisitor(any());
        Mockito.when(visitorMock.getID()).thenReturn(ID);
        Mockito.doReturn(null).when(cmd_spy).getVisitor(not(eq(ID)));
        Mockito.doReturn(borrowedList).when(cmd_spy).getBorrowedBooks(any());
        Mockito.doReturn(" ").when(cmd_spy).getBookResponse(any(), any());
    }

    @Test
    public void testNoInput() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: returns ResponseFlag.SUCCESS, but no borrowed books will
         be queried and printed out
         */
        assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(execMock, ""));
        Mockito.verify(cmd_spy, Mockito.never()).getVisitor(any());
        Mockito.verify(cmd_spy, Mockito.never()).getBorrowedBooks(any());
        Mockito.verify(execMock).sendMessage("borrowed,missing-parameters,visitor-id;");
    }

    @Test
    public void testWrongIDType() {
         /*
        Test Explanation: Testing non-long visitor ID input
        Expectation:  FAILURE response flag, but no borrowed books will be
        printed out
         */
        Assertions.assertSame(ResponseFlag.FAILURE,
                cmd_spy.onExecute(this.execMock, "@%"));
        Mockito.verify(cmd_spy, never()).getVisitor(any());
        Mockito.verify(cmd_spy, never()).getBorrowedBooks(any());
    }

    @Test
    public void invalidVisitorID() {
         /*
        Test Explanation: Testing Visitor ID that is not in the library database
        Expectation: SUCCESS response flag, but no borrowed books will be
        queried and printed out
         */
        Assertions.assertSame(ResponseFlag.SUCCESS,
                cmd_spy.onExecute(this.execMock, INVALID_VISITOR_ID));
        Mockito.verify(cmd_spy, times(1)).getVisitor(any());
        Mockito.verify(cmd_spy, never()).getBorrowedBooks(any());
        Mockito.verify(execMock).sendMessage("borrowed,invalid-visitor-id;");
    }

    @Test
    public void visitorHasNoBooks() {
        /*
        Test Explanation: test the command with everything accurate. The
        visitor has no borrowed books under her account
        Expectation: returns ResponseFlag.SUCCESS, no books being printed out.
         */
        assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(execMock,
                VALID_VISITOR_ID));
        Mockito.verify(cmd_spy, times(1)).getVisitor(any());
        Mockito.verify(cmd_spy, Mockito.times(1)).getBorrowedBooks(any());
        Mockito.verify(cmd_spy, never()).getBookResponse(any(), any());
        Mockito.verify(execMock).sendMessage("borrowed,0;");
    }

    @Test
    public void visitorHasBorrowedBooks() {
        /*
        Test Explanation: test the command with everything accurate. The
        visitor has borrowed books under her account
        Expectation: returns ResponseFlag.SUCCESS, all borrowed books being
        printed out.
         */
        this.borrowedList.add(checkoutMock);
        assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(execMock,
                VALID_VISITOR_ID));
        Mockito.verify(cmd_spy, times(1)).getVisitor(any());
        Mockito.verify(cmd_spy, Mockito.times(1)).getBorrowedBooks(any());
        Mockito.verify(cmd_spy, Mockito.times(1)).getBookResponse(any(), any());
    }

}
