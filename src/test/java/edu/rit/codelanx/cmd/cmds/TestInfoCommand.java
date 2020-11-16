package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.loader.Query;
import edu.rit.codelanx.data.state.types.*;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestInfoCommand {

    private final String ISBN = "9780545227247";
    private final String TITLE = "Catching Fire (The Second Book of the Hunger Games)";
    private final String AUTHOR = "Suzanne Collins";
    private final String PUBLISHER = "Scholastic Inc.";
    private final Long BOOK_ID = 1L;

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;

    InfoCommand cmd;
    InfoCommand cmdSpy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.cmd = new InfoCommand(this.servMock);
        this.cmdSpy = Mockito.spy(cmd);
    }

    @Test
    public void wrongSortOrder(){
        /*
        Test Explanation: Testing sending info an incorrect sorting order
        Expectation: All inputs should be able to be handled, no info displayed
        */
        Author authorMock = Mockito.mock(Author.class);
        List<Author> authorList = Collections.<Author>singletonList(authorMock);
        Mockito.doReturn(authorList).when(cmdSpy).findAuthors(Matchers.any());
        Mockito.doReturn(Collections.<Long>singleton(BOOK_ID)).when(cmdSpy).getIDs(Matchers.any());
        Book book = Mockito.mock(Book.class);
        Mockito.doReturn(Stream.of(book)).when(cmdSpy).getBookStream(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any());
        Assertions.assertSame(ResponseFlag.SUCCESS, cmdSpy.onExecute(execMock, TITLE, AUTHOR, ISBN, PUBLISHER, "page-count"));
        Mockito.verify(execMock).sendMessage("info,invalid-sort-order;");
    }
    @Test
    public void happyPath() {
        /*
        Test Explanation: Testing sending info a fully correct command
        Expectation: All inputs should be able to be handled, book info should be displayed
        */
        Author authorMock = Mockito.mock(Author.class);
        List<Author> authorList = Collections.<Author>singletonList(authorMock);
        Mockito.doReturn(authorList).when(cmdSpy).findAuthors(Matchers.any());
        Mockito.doReturn(Collections.<Long>singleton(BOOK_ID)).when(cmdSpy).getIDs(Matchers.any());
        Mockito.doReturn(Mockito.mock(Query.class)).when(cmdSpy).bookQuery();
        Book book = Mockito.mock(Book.class);
        Mockito.doReturn(TITLE).when(book).getTitle();
        Mockito.doReturn(Stream.of(book)).when(cmdSpy).getBookStream(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any());
        Assertions.assertSame(ResponseFlag.SUCCESS, cmdSpy.onExecute(execMock, TITLE, AUTHOR, ISBN, PUBLISHER, "title"));
    }
}
