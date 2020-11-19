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

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.*;


public class TestDepartCommand {
    private final String VALID_VISITOR_ID = "7";
    private long id = 7;
    private long invalid_id=2;
    private final String INVALID_VISITOR_ID = "2";
    private DepartCommand cmd;
    private DepartCommand cmd_spy;


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
        cmd = new DepartCommand(this.servMock);
        cmd_spy = Mockito.spy(cmd);

        //Stubbing methods
        Mockito.when(servMock.getLibraryData()).thenReturn(dataSourceMock);
        Mockito.when(dataSourceMock.getLibrary()).thenReturn(libraryMock);
        Mockito.when(libraryMock.isOpen()).thenReturn(true);
        Mockito.doReturn(visitorMock).when(cmd_spy).getVisitor(id);
        Mockito.when(visitorMock.getID()).thenReturn(id);
        Mockito.when(visitorMock.isVisiting()).thenReturn(true);
        Mockito.doReturn(null).when(cmd_spy).getVisitor(not(eq(id)));
        Mockito.doReturn("depart," + id + ",06:09:06,07:10:07").when(cmd_spy).endVisit(any());


    }

    @Test
    public void testNoInput() {
         /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: return SUCCESS response flag, but departing behaviour doesn't occur
         */
        //Assertions.assertEquals(ResponseFlag.SUCCESS, cmd.onExecute(this.execMock, " "));
        //Mockito.verify(cmd_spy, never()).getVisitor(any());
        //Mockito.verify(execMock).sendMessage("depart,missing-parameters,visitor-id");
    }

    @Test
    public void testWrongIDType() {
         /*
        Test Explanation: Testing non-long visitor ID input
        Expectation: return FAILURE response flag, departing behaviour doesn't occur
         */
        Assertions.assertSame(ResponseFlag.FAILURE, cmd.onExecute(this.execMock, "henlo"));
        Assertions.assertSame(ResponseFlag.FAILURE, cmd.onExecute(this.execMock, "13.5"));
        Assertions.assertSame(ResponseFlag.FAILURE, cmd.onExecute(this.execMock, "@%"));
        Mockito.verify(cmd_spy, never()).getVisitor(any());
    }

    @Test
    public void testHappyPath() {
          /*
        Test Explanation: depart a valid visitor
        Expectation: return SUCCESS response flag
         */
        Assertions.assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, VALID_VISITOR_ID));
        Mockito.verify(cmd_spy, times(1)).getVisitor(id);
        Mockito.verify(cmd_spy, times(1)).endVisit(visitorMock);
        Mockito.verify(execMock).sendMessage("depart," + id + ",06:09:06,07:10:07");
    }
    @Test
    public void departInvalidVisitor() {
         /*
        Test Explanation: try to depart a visitor whose ID cannot be found in the library database (haven't registered)
        Expectation: return SUCCESS response flag, but departing behaviour doesn't occur
         */
        Assertions.assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, INVALID_VISITOR_ID));
        Mockito.verify(cmd_spy, times(1)).getVisitor(invalid_id);
        Mockito.verify(cmd_spy, never()).endVisit(any());
        Mockito.verify(execMock).sendMessage("depart,invalid-id;");
    }

    @Test
    public void departNonVisitingVisitor() {
         /*
        Test Explanation: try to depart a visitor with valid ID but not currently visiting
        Expectation: return SUCCESS response flag, but departing behaviour doesn't occur
         */
        Mockito.when(visitorMock.isVisiting()).thenReturn(false);
        Assertions.assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, VALID_VISITOR_ID));
        Mockito.verify(cmd_spy, times(1)).getVisitor(any());
        Mockito.verify(cmd_spy, never()).endVisit(any());
        Mockito.verify(execMock).sendMessage("depart,invalid-id;");
    }

}
