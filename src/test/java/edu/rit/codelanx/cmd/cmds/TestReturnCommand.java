package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Matchers.any;

public class TestReturnCommand {

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;
    @Mock
    private Visitor visitorMock;

    private ReturnCommand ret;
    private ReturnCommand retSpy;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.ret = new ReturnCommand(this.servMock);
        retSpy = Mockito.spy(ret);
    }

    @Test
    public void testNoInput() {
        /*
        Test Explanation: Testing report when passed empty values
        Expectation: All inputs should be able to be handled, no book should
        be returned
         */
        Assertions.assertSame(ResponseFlag.SUCCESS,
                retSpy.onExecute(this.execMock, "", ""));
        Mockito.verify(execMock).sendMessage("return,missing-parameters," +
                "visitor,id;");
    }

    @Test
    public void invalidVisitorID() {
        /*
        Test Explanation: Testing report when passed an incorrect visitor id
        Expectation: All inputs should be able to be handled, no book should
        be returned
         */
        Assertions.assertSame(ResponseFlag.SUCCESS,
                retSpy.onExecute(this.execMock, "2", "1"));
        Mockito.verify(execMock).sendMessage("return,invalid-visitor-id;");
    }

    @Test
    public void invalidBookID() {
        /*
        Test Explanation: Testing report when passed an invalid book id
        Expectation: All inputs should be able to be handled, no book should
        be returned
         */
        Mockito.doReturn(visitorMock).when(retSpy).queryVisitor(any());
        Mockito.doReturn(Collections.<Book>emptySet()).when(retSpy).queryBooks(any());
        Assertions.assertSame(ResponseFlag.SUCCESS,
                retSpy.onExecute(this.execMock, "1", "1"));
        Mockito.verify(execMock).sendMessage("return,invalid-book-id,2");
    }
}
