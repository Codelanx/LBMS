package edu.rit.codelanx.util;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.AuthorListing;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.StateType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

//TODO: Documentation
public enum BookParser {;

    private static final String BOOKS_FILE = "/books.txt";
    private static final int BOOK_COUNT = 515;
    /** The {@link DateTimeFormatter} for yyyy/mm/dd format */
    public static final DateTimeFormatter DATE_FORMAT;

    static {
        DATE_FORMAT = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR)
                .optionalStart()
                    .appendLiteral('-')
                    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                    .optionalStart()
                        .appendLiteral('-')
                        .appendValue(ChronoField.DAY_OF_MONTH, 2)
                    .optionalEnd()
                .optionalEnd()
                .parseDefaulting(ChronoField.NANO_OF_DAY, 0) //for parsing dates to instants
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .toFormatter()
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault());
    }

    //9780674028678,"The Race Between Education and Technology",{Claudia Dale Goldin, Lawrence F. Katz},"Harvard University Press",2008,488
    //9781591987628,"Build-a-Skill Instant Books: Synonyms and Antonyms, Gr. Kâ€“1, eBook",{Trisha Callella},"Creative Teaching Press",2007-01-01,32
    public static List<Book> parseBooks(DataSource storage) {
        List<Book> back = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(BookParser.class.getResourceAsStream(BOOKS_FILE));
             BufferedReader br = new BufferedReader(isr)) {
            String s;
            while ((s = br.readLine()) != null) {
                BookParser.parseAndInsert(storage, s);
            }
        } catch (IOException e) {
            Errors.report(e);
        }
        return back;
    }

    public static Map<State.Type, List<? extends State>> parseAndInsert(DataSource insertInto, String line) {
        Map<State.Type, List<? extends State>> back = new HashMap<>();
        StateBuilder<Book> book = Book.create();
        int start = 0, end = line.indexOf(',');
        book.setValue(Book.Field.ISBN, line.substring(start, end));
        start = end+2; //skip ,"
        end = line.indexOf('"', start);
        book.setValue(Book.Field.TITLE, line.substring(start, end));
        start = end+3; //skip ",{
        end = line.indexOf('}', start);
        List<Author> authors = Arrays.stream(line.substring(start, end).split(","))
                                    .map(String::trim)
                                    .map(s -> BookParser.findOrInsertAuthor(insertInto, s))
                                    .collect(Collectors.toList());
        back.put(StateType.AUTHOR, authors);
        start = end+3; //skip },"
        end = line.indexOf('"', start);
        book.setValue(Book.Field.PUBLISHER, line.substring(start, end));
        start = end+2; //skip ",
        end = line.indexOf(',', start);
        book.setValue(Book.Field.PUBLISH_DATE, BookParser.DATE_FORMAT.parse(line.substring(start, end), Instant::from));
        book.setValue(Book.Field.PAGE_COUNT, Integer.parseInt(line.substring(end+1)));
        book.setValue(Book.Field.CHECKED_OUT, -1);
        book.setValue(Book.Field.TOTAL_COPIES, -1);
        Book addedBook = book.build(insertInto);
        back.put(addedBook.getType(), Collections.singletonList(addedBook));
        List<AuthorListing> listings = authors.stream()
                .map(author -> {
                    return AuthorListing.create()
                            .setValue(AuthorListing.Field.AUTHOR, author)
                            .setValue(AuthorListing.Field.BOOK, addedBook)
                            .build(insertInto);
                })
                .collect(Collectors.toList());
        back.put(StateType.AUTHOR_LISTING, listings);
        return back;
    }

    private static Author findOrInsertAuthor(DataSource source, String name) {
        Author back = source.query(Author.class).isEqual(Author.Field.NAME, name).results().findAny().orElse(null);
        if (back == null) {
            back = Author.create().setValue(Author.Field.NAME, name).build(source);
        }
        return back;
    }
}
