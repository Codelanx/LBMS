package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.storage.StorageContainer;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static edu.rit.codelanx.data.storage.field.FieldModifier.*;

/**
 * represents a book's list of authors.
 *
 * @author sja9291  Spencer Alderman
 */
@StorageContainer("book_authors")
public class AuthorListing extends BasicState {

    public static class Field {

        public static final DataField<Long> ID; //hmmmm
        public static final DataField<Author> AUTHOR;
        public static final DataField<Book> BOOK;
        private static final DataField<? super Object>[] VALUES;


        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, AUTHOR, BOOK };
        }

        static {
            ID = DataField.makeIDField(AuthorListing.class);
            AUTHOR = DataField.buildFromState(Author.class, "author", Author.Field.ID, FM_IMMUTABLE, FM_KEY);
            BOOK = DataField.buildFromState(Book.class, "book", Book.Field.ID, FM_IMMUTABLE, FM_KEY);
            VALUES = Field.values();
        }
    }

    AuthorListing(DataStorage loader, long id, StateBuilder<AuthorListing> builder) {
        super(loader, id, builder);
    }
    public AuthorListing(DataStorage loader, ResultSet sql) throws SQLException {
        super(loader, sql);
    }
    public AuthorListing(DataStorage loader, Map<String, Object> file) {
        super(loader, file);
    }

    @Override
    protected DataField<? super Object>[] getFieldUnsafe() {
        return Field.VALUES;
    }

    @Override
    public DataField<Long> getIDField() {
        return Field.ID;
    }

    @Override
    public DataField<? super Object>[] getFields() {
        return Field.values();
    }

    @Override
    public Type getType() {
        return StateType.AUTHOR_LISTING;
    }

    public Author getAuthor() {
        return Field.AUTHOR.get(this);
    }

    public Book getBook() {
        return Field.BOOK.get(this);
    }

    @Override
    public String toFormattedText() {
        return this.getAuthor().toFormattedText();
    }

    public static StateBuilder<AuthorListing> create() {
        return StateBuilder.of(AuthorListing::new, StateType.AUTHOR_LISTING, Field.VALUES);
    }

}
