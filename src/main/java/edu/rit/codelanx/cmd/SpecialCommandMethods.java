package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.types.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface SpecialCommandMethods {


    public List<Book> getBooks(String title, String isbn, String publisher,
                               String sortOrder, Author... authors);

    //Need a way to get the list of books that the visitor has checked out
    public List<Book> getCheckedOut();

    //Need a way to check the books out to the visitor, should change both
    // the database and add to the list of checked out books by the visitor
    public void checkOut(Visitor v);

    //Need another field for book that holds it's last checkout date

    //Need a way to get the Instant for the time that the visitor started
    // their visit
    public Instant getVisitStart();

    public void pay(Library library, Visitor visitor, BigDecimal amount);

    public int totalRegisteredVisitors(List<Visitor> numVisitors);


}
