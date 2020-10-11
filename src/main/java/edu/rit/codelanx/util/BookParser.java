package edu.rit.codelanx.util;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.types.Book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public enum BookParser {;

    private static final String BOOKS_FILE = "books.txt";
    private static final int BOOK_COUNT = 515;

    //TODO: data preconditional - when inserting, insert things without any form of external mappings
    //TODO:     E.g. insert: [Author, Visitor, Library, Book] -> [AuthorListing, Visit, Checkout, Transaction]
    //TODO: This will ensure that relationships are intact, as the latter will search for the former while being made

    //9780674028678,"The Race Between Education and Technology",{Claudia Dale Goldin, Lawrence F. Katz},"Harvard University Press",2008,488
    //9781591987628,"Build-a-Skill Instant Books: Synonyms and Antonyms, Gr. Kâ€“1, eBook",{Trisha Callella},"Creative Teaching Press",2007-01-01,32
    public static List<Book> parseBooks(DataStorage storage) {
        List<Book> back = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(BookParser.class.getResourceAsStream(BOOKS_FILE));
             BufferedReader br = new BufferedReader(isr)) {
            String s;
            while ((s = br.readLine()) != null) {
                TempContainer temp = new TempContainer();
                //TODO: Fix below, compiler error
                //temp.builder = Book.create(storage).checkedOut(0).totalCopies(1); //TODO: More than 1 copy???
                BookParser.parseBook(temp, s);
                //TODO: Insert book to storage
            }
        } catch (IOException e) {
            Errors.report(e);
        }
        return back;
    }

    public static class TempContainer {
        public StateBuilder<Book> builder;
        public String[] authors;
    }

    //TODO: Fix
    public static StateBuilder<Book> parseBook(TempContainer temp, String s) {
    /*    int start = 0, end = s.indexOf(',');
        temp.builder.setValue(Book.Field.ISBN, s.substring(start, end));
        start = end+2; //skip ,"
        end = s.indexOf('"', start);
        temp.builder.setValue(Book.Field.TITLE, s.substring(start, end));
        start = end+3; //skip ",{
        end = s.indexOf('}', start);
        //TODO: Figure out what to do with authors
        //TODO: We know now, we have to insert author objects and the listings!
        temp.authors = Arrays.stream(s.substring(start, end).split(","))
                .map(String::trim).toArray(String[]::new);
        start = end+3; //skip },"
        end = s.indexOf('"', start);
        temp.builder.publisher(s.substring(start, end));
        start = end+2; //skip ",
        end = s.indexOf(',', start);
        //temp.builder.publishDate(s.substring(start, end)); //TODO: Fix
        temp.builder.pageCount(Integer.parseInt(s.substring(end+1)));
        return temp.builder;

     */
        return null;
    }
}
