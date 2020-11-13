package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;

public class TestBorrowCommand {

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
    @Mock
    private Book bookMock;

    private List<Book> bookSet = new ArrayList<>();
    private BorrowCommand borSpy;
    private BorrowCommand bor;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.bor = new BorrowCommand(servMock);
        this.borSpy = Mockito.spy(bor);
        this.bookSet.add(bookMock);

        Mockito.when(servMock.getLibraryData()).thenReturn(libDSMock);
        Mockito.when(libDSMock.getLibrary()).thenReturn(libMock);
        Mockito.when(libMock.isOpen()).thenReturn(true);
        Mockito.doReturn(visitorMock).when(borSpy).getVisitor(any());
    }

    @Test
    public void testNoInput() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: All inputs should be able to be handled, no books will be checked out
         */
        assertSame(ResponseFlag.SUCCESS, borSpy.onExecute(execMock, "",""));
        Mockito.verify(borSpy, Mockito.never()).getBooks(anySet());
        Mockito.verify(borSpy, Mockito.never()).getCheckedOut(any());
        Mockito.verify(borSpy, Mockito.never()).checkout(any(), any());
        Mockito.verify(execMock).sendMessage("borrow,missing-parameters,visitor-id,id;");
    }

    @Test
    public void tooManyCheckedOut() {
        /*
        Test Explanation: Testing a user trying to check out a book with too many books checked out
        Expectation: All inputs should be able to be handled, no books should be checked out
         */
        Mockito.doReturn(5L).when(borSpy).getCheckedOut(any());
        assertSame(ResponseFlag.SUCCESS, borSpy.onExecute(execMock, "1", "1"));
        assertSame(ResponseFlag.SUCCESS, borSpy.onExecute(execMock, "1", "1", "2", "3", "4", "5", "6"));
        Mockito.verify(borSpy, Mockito.never()).checkout(any(), any());
        Mockito.verify(execMock, Mockito.times(2)).sendMessage("borrow,book-limit-exceeded;");
    }

    @Test
    public void invalidBookID(){
        /*
        Test Explanation: Testing a user passing a book id that doesn't exist
        Expectation: All inputs should be able to be handled, no books should be checked out
         */
        Mockito.doReturn(BigDecimal.ZERO).when(visitorMock).getMoney();
        Mockito.doReturn(0L).when(borSpy).getCheckedOut(any());
        List<Book> emptyBookList = new ArrayList<>();
        Mockito.doReturn(emptyBookList).when(borSpy).getBooks(any());
        assertSame(ResponseFlag.SUCCESS, borSpy.onExecute(execMock, "1", "656345"));
        Mockito.verify(borSpy, Mockito.never()).checkout(any(), any());
        Mockito.verify(execMock).sendMessage("borrow,invalid-book-id,656345;");
    }

    @Test
    public void outstandingFine(){
        /*
        Test Explanation: Testing a user trying to check out a book with an outstanding fine
        Expectation: All inputs should be able to be handled, no books checked out
         */
        Mockito.doReturn(BigDecimal.valueOf(-1L)).when(visitorMock).getMoney();
        Mockito.doReturn(0L).when(borSpy).getCheckedOut(any());
        assertSame(ResponseFlag.SUCCESS, borSpy.onExecute(execMock, "1", "1"));
        Mockito.verify(borSpy, Mockito.never()).getBooks(any());
        Mockito.verify(borSpy, Mockito.never()).checkout(any(), any());
        Mockito.verify(execMock).sendMessage("borrow,outstanding-fine,-1;");
    }

    @Test
    public void happyPath(){
        /*
        Test Explanation: Testing a user checking out a book with everything being correct
        Expectation: All inputs should be able to be handled, books checked out
         */
        Mockito.doReturn(BigDecimal.ZERO).when(visitorMock).getMoney();
        Mockito.doReturn(0L).when(borSpy).getCheckedOut(any());
        Mockito.doReturn(bookSet).when(borSpy).getBooks(any());
        Mockito.doReturn(1).when(bookMock).getAvailableCopies();
        Mockito.doNothing().when(borSpy).checkout(any(), any());
        Instant time = Instant.now();
        Mockito.doReturn(time).when(borSpy).getDueDate();
        assertSame(ResponseFlag.SUCCESS, borSpy.onExecute(execMock, "1", "1"));
        Mockito.verify(borSpy, Mockito.times(1)).checkout(any(), any());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        String formatted = dtf.format(now);
        Mockito.verify(execMock).sendMessage("borrow," + formatted + ";");
    }
}
