package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Matchers.any;

public class TestSearchCommand {
    String[] validAuthors = {"Carter", "Josh"};
    @Mock
    List<Author> authorList;
    @Mock
    Set<Long> filterIDs;
    ///
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

    SearchCommand cmd;
    SearchCommand cmd_spy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cmd = new SearchCommand(this.servMock);
        this.cmd_spy = Mockito.spy(cmd);

        Mockito.when(servMock.getLibraryData()).thenReturn(DataSourceMock);
        Mockito.when(DataSourceMock.getLibrary()).thenReturn(libMock);
        Mockito.when(servMock.getBookStore()).thenReturn(DataSourceMock);

        Mockito.when(cmd_spy.findAuthors(validAuthors)).thenReturn(authorList);
        Mockito.when(cmd_spy.getFilterIDs(authorList)).thenReturn(filterIDs);
        Mockito.doReturn(" ").when(cmd_spy).output(execMock, any());
        Mockito.when(libMock.isOpen()).thenReturn(true);
    }


    @Test
    public void     happyPath() {
        assertSame(ResponseFlag.SUCCESS, cmd_spy.execute(execMock,"title", "12212",
                "publisher", "title", validAuthors));
        Mockito.verify(cmd_spy, Mockito.times(1)).output(any(), any());
    }
}
