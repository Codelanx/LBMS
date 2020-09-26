package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.types.Author;
import edu.rit.codelanx.data.types.Book;

import java.util.List;

public interface SpecialCommandMethods {

    public List<Book> getBooks(String title, String isbn, String publisher, String sortOrder, Author... authors);
}
