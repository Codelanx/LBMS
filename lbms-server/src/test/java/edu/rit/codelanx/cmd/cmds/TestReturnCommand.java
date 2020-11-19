package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Collections;

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
        this.retSpy = Mockito.spy(ret);

    }


    @Test
    public void invalidVisitorID() {
        /*
        Test Explanation: Testing report when passed an incorrect visitor id
        Expectation: All inputs should be able to be handled, no book should
        be returned
         */
        Mockito.doReturn(null).when(retSpy).queryVisitor(Matchers.anyLong());
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
        Mockito.doReturn(visitorMock).when(retSpy).queryVisitor(Matchers.anyLong());
        Mockito.doReturn(Collections.<Book>emptySet()).when(retSpy).queryBooks(Mockito.any());
        Assertions.assertSame(ResponseFlag.SUCCESS,
                retSpy.onExecute(this.execMock, "1", "1"));
        Mockito.verify(execMock).sendMessage("return,invalid-book-id,1;");
    }

    @Test
    public void happyPath(){
        Mockito.doReturn(visitorMock).when(retSpy).queryVisitor(Matchers.anyLong());
        Book book = Mockito.mock(Book.class);
        Checkout checkout = Mockito.mock(Checkout.class);
        Mockito.doReturn(Collections.<Book>singleton(book)).when(retSpy).queryBooks(Mockito.any());
        Mockito.doReturn(Collections.<Checkout>singletonList(checkout)).when(retSpy).queryCheckouts(Matchers.any(), Matchers.any());
        Assertions.assertSame(ResponseFlag.SUCCESS, retSpy.onExecute(this.execMock, "1", "1"));
        Mockito.verify(execMock).sendMessage("return,success;");
    }
}
