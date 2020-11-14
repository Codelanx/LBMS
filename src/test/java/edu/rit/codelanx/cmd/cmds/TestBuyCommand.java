package edu.rit.codelanx.cmd.cmds;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.LibraryData;
import edu.rit.codelanx.data.loader.MemoryStorageAdapter;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.network.server.TextServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TestBuyCommand {

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;

    private BuyCommand buy;
    private BuyCommand buySpy;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.buy = new BuyCommand(this.servMock);
        this.buySpy = Mockito.spy(buy);
    }

    @Test
    public void testNoQuantity(){
        /*
        Test Explanation: Testing sending an empty input for the quantity
        Expectation: All inputs should be able to be handled, no books should be bought
        */
        Assertions.assertSame(ResponseFlag.SUCCESS, buySpy.onExecute(execMock, ""));
        Mockito.verify(execMock).sendMessage("buy,missing-parameters,quantity;");
    }

    @Test
    public void testNoBooks(){
        /*
        Test Explanation: Testing sending 0 books to purchase
        Expectation: All inputs should be able to be handled, no books should be bought
        */
        Assertions.assertSame(ResponseFlag.SUCCESS, buySpy.onExecute(execMock, "0"));
        Mockito.verify(execMock).sendMessage("buy,success,0;");
    }

    @Test
    public void incorrectBookID(){
        /*
        Test Explanation: Testing sending an incorrect book ID
        Expectation: Should send a failure flag, no books should be bought
        */
        Mockito.doReturn(null).when(buySpy).queryBookstore(Matchers.anyLong());
        Mockito.doReturn(null).when(buySpy).queryLibrary(Matchers.anyLong());
        Assertions.assertSame(ResponseFlag.FAILURE, buySpy.onExecute(execMock, "1", "7"));
        Assertions.assertSame(ResponseFlag.FAILURE, buySpy.onExecute(execMock, "1", "A"));
        Assertions.assertSame(ResponseFlag.FAILURE, buySpy.onExecute(execMock, "1", "-1"));
    }

    /*@Mock
    private static final Server<TextMessage> SERVER = Mockito.mock(TextServer.class, Mockito.CALLS_REAL_METHODS);
    @Mock
    private static final CommandExecutor EXECUTOR = Mockito.mock(CommandExecutor.class);

    private static final DataSource STORAGE = new LibraryData(MemoryStorageAdapter::new);
    private static final BuyCommand COMMAND = new BuyCommand(SERVER);
    private static Book BOOKSTORE_BOOK;

    @BeforeEach
    public void init(){
        MockitoAnnotations.initMocks(this);
        Mockito.when(SERVER.getLibraryData()).thenReturn(STORAGE);
        BOOKSTORE_BOOK = SERVER.getBookStore().query(Book.class)
                .results().findAny()
                .orElseThrow(() -> new ExceptionInInitializerError("Bad Test - Empty bookstore"));
    }

    @Test
    public void testFirstBuy() {
        STORAGE.query(Book.class) //remove this book for our test
                .isEqual(Book.Field.ISBN, BOOKSTORE_BOOK.getISBN())
                .results().findAny().ifPresent(STORAGE.getAdapter()::remove);
        ResponseFlag flag = COMMAND.execute(EXECUTOR, 1, BOOKSTORE_BOOK.getID());
        Assertions.assertEquals(ResponseFlag.SUCCESS, flag);
        Book added = TestBuyCommand.getLibraryBook().orElse(null);
        Assertions.assertNotNull(added);
        Assertions.assertSame(1, added.getTotalCopies());
        TestBuyCommand.checkSuccessMessage(added);
    }

    @Test
    public void testDuplicateBuy() {
        int old = TestBuyCommand.getLibraryBook().map(Book::getTotalCopies).orElse(0);
        int add = ThreadLocalRandom.current().nextInt(100);
        ResponseFlag flag = COMMAND.execute(EXECUTOR, add, BOOKSTORE_BOOK.getID());
        Assertions.assertEquals(ResponseFlag.SUCCESS, flag);
        Book b = TestBuyCommand.getLibraryBook().orElse(null);
        Assertions.assertNotNull(b);
        Assertions.assertEquals(old + add, b.getTotalCopies());
        TestBuyCommand.checkSuccessMessage(b);
    }

    private static Optional<Book> getLibraryBook() {
        return STORAGE.query(Book.class)
                .isEqual(Book.Field.ISBN, BOOKSTORE_BOOK.getISBN())
                .results().findAny();
    }

    private static void checkSuccessMessage(Book... books) {
        InOrder order = Mockito.inOrder(EXECUTOR);
        order.verify(EXECUTOR).sendMessage("buy,success,1,[\n");
        for (Book b : books) {
            String out = String.format("%s,%s,{%s},%s,%d\n",
                    b.getISBN(), b.getTitle(),
                    b.getAuthors().map(Author::getName).collect(Collectors.joining(",")),
                    TextCommand.DATE_FORMAT.format(b.getPublishDate()), 1);
            order.verify(EXECUTOR).sendMessage(out);
        }
        order.verify(EXECUTOR).sendMessage("];");
        order.verifyNoMoreInteractions();
    }*/

}
