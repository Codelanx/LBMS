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
import static org.mockito.Matchers.*;

public class TestArriveCommand {

    private final String VALID_VISITOR_ID = "5";
    private final String INVALID_VISITOR_ID = "3";

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;
    @Mock
    private DataSource libDSMock;
    @Mock
    private Library libMock;
    @Mock
    private Visitor visitorMock;

    private ArriveCommand arrSpy;
    private ArriveCommand arr;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.arr = new ArriveCommand(this.servMock);
        arrSpy = Mockito.spy(arr);

        //Stubbing methods so the command can run
        Mockito.when(servMock.getLibraryData()).thenReturn(libDSMock);
        Mockito.when(libDSMock.getLibrary()).thenReturn(libMock);
        Mockito.when(libMock.isOpen()).thenReturn(true);
        Mockito.doReturn(visitorMock).when(arrSpy).getVisitor(5L);
        Mockito.doReturn(null).when(arrSpy).getVisitor(not(eq(5L)));
        Mockito.doReturn("").when(arrSpy).startVisit(any());

        Mockito.when(visitorMock.getID()).thenReturn(5L);Mockito.when(visitorMock.isVisiting()).thenReturn(false);
        Mockito.when(visitorMock.getID()).thenReturn(5L);
    }

    @Test
    public void tooMuchInput() {
        /*
        Test Explanation: Testing sending too much input to the command
        Expectation: All inputs should be able to be handled, and visitor should arrive if their id is correct
         */
        //Mockito.when(visitorMock.isVisiting()).thenReturn(false);
        //Assertions.assertSame(ResponseFlag.SUCCESS, this.arrSpy.onExecute(this.execMock, INVALID_VISITOR_ID, "2"));
        //Mockito.verify(execMock).sendMessage("arrive,invalid-id;");
        //Assertions.assertSame(ResponseFlag.SUCCESS, this.arrSpy.onExecute(this.execMock, VALID_VISITOR_ID, "7"));
        //Mockito.verify(arrSpy, Mockito.times(1)).startVisit(any());
    }

    @Test
    public void wrongIDType() {
        /*
        Test Explanation: Try to pass in a non-long value as visitorID
        Expectation: All inputs should not be able to be handled, and will return a failure flag
        */
        Mockito.when(visitorMock.isVisiting()).thenReturn(false);
        Assertions.assertSame(ResponseFlag.FAILURE, this.arrSpy.onExecute(this.execMock, "A"));
        Assertions.assertSame(ResponseFlag.FAILURE, this.arrSpy.onExecute(this.execMock, "$@!#$%"));
        Assertions.assertSame(ResponseFlag.FAILURE, this.arrSpy.onExecute(this.execMock, "5A"));
        Mockito.verify(arrSpy, Mockito.times(0)).startVisit(any());
        Mockito.verify(arrSpy, Mockito.times(0)).getVisitor(anyLong());
    }

    @Test
    public void duplicateVisitStart() {
        /*
        Test Explanation: Trying to have a visitor arrive who is already visiting
        Expectation: All input should be able to be handled, and should return ResponseFlag.Success;
        */
        Mockito.when(visitorMock.isVisiting()).thenReturn(true);
        Assertions.assertSame(ResponseFlag.SUCCESS, this.arrSpy.onExecute(this.execMock, VALID_VISITOR_ID));
        Mockito.verify(arrSpy, Mockito.times(0)).startVisit(any());
        Mockito.verify(execMock).sendMessage("arrive,duplicate;");
    }
}
