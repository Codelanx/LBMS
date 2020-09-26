package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.types.Author;
import edu.rit.codelanx.data.types.Book;
import edu.rit.codelanx.data.types.Visit;
import edu.rit.codelanx.data.types.Visitor;

import java.util.List;

public interface SpecialCommandMethods {

    public List<Book> getBooks(String title, String isbn, String publisher,
                               String sortOrder, Author... authors);

    //Need a way to get the list of books that the visitor has checked out
    public List<Book> getCheckedOut(Visitor v);

    //Need a way to check the books out to the visitor, should change both
    // the database and add to the list of checked out books by the visitor
    public void checkOut(Visitor v);
}
