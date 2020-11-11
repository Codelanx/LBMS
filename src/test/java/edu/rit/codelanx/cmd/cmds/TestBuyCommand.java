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
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TestBuyCommand {

    private static final DataSource STORAGE = new LibraryData(MemoryStorageAdapter::new);
    private static final Server<TextMessage> SERVER = Mockito.mock(TextServer.class, Mockito.CALLS_REAL_METHODS);
    private static final CommandExecutor EXECUTOR = Mockito.mock(CommandExecutor.class);
    private static final BuyCommand COMMAND = new BuyCommand(SERVER);
    private static final Book BOOKSTORE_BOOK;

    static {
        Mockito.when(SERVER.getLibraryData()).thenReturn(STORAGE);
        BOOKSTORE_BOOK = SERVER.getBookStore().query(Book.class)
                .results().findAny()
                .orElseThrow(() -> new ExceptionInInitializerError("Bad Test - Empty bookstore"));
    }

    @Test
    public static void testFirstBuy() {
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
    public static void testDuplicateBuy() {
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
    }

}
