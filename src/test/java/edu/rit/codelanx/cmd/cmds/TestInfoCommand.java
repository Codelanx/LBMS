package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.loader.Query;
import edu.rit.codelanx.data.state.types.*;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.sound.sampled.Line;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;

public class TestInfoCommand {
    private final String VALID_VISITOR_ID = "7";
    private long ID = 7;
    private final String INVALID_VISITOR_ID = "2";
    String[] validAuthors = {"Carter", "Josh"};

    @Mock
    Query<Book> query;
    @Mock
    List<Author> authorList;
    @Mock
    Set<Long> filterIDs;
    @Mock
    Stream<Book> res;

    @Mock
    List<Book> bookList;

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

    InfoCommand cmd;
    InfoCommand cmd_spy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cmd = new InfoCommand(this.servMock);
        this.cmd_spy = Mockito.spy(cmd);

        //Stubbing methods
        Mockito.when(servMock.getLibraryData()).thenReturn(DataSourceMock);
        Mockito.when(DataSourceMock.getLibrary()).thenReturn(libMock);
        Mockito.when(libMock.isOpen()).thenReturn(true);
        //query database for the author list
        Mockito.doReturn(authorList).when(cmd_spy).findAuthors(validAuthors);
        Mockito.doReturn(null).when(cmd_spy).findAuthors(not(eq(validAuthors)));

        //query db for set<long> ids based on author list
        Mockito.doReturn(filterIDs).when(cmd_spy).getIDs(any());
        //Mockito.when(cmd_spy.getIDs(authorList)).thenReturn(filterIDs);
        Mockito.doReturn(null).when(cmd_spy).getIDs(not(eq(authorList)));

        //query for books indb
        Mockito.when(cmd_spy.bookQuery()).thenReturn(query);

        //find books by author
        //Mockito.when(cmd_spy.findBookByAuthor(filterIDs, query, filterIDs)).thenReturn(res);
        Mockito.doReturn(null).when(cmd_spy).getIDs(not(eq(authorList)));

        //res-> List<Book>
        Mockito.doReturn(" ").when(cmd_spy).outputInfo(any());

        Mockito.when(visitorMock.isVisiting()).thenReturn(true);

    }

    @Test
    public void happyPath() {
        assertSame(ResponseFlag.SUCCESS, cmd_spy.execute(execMock, "bruh"
                , "212232", "publisher", "title", validAuthors));
        Mockito.verify(cmd_spy, Mockito.times(1)).findAuthors(validAuthors);
        Mockito.verify(cmd_spy, Mockito.times(1)).outputInfo(any());
    }
}
