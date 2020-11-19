package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.field.FieldIndicies;
import edu.rit.codelanx.data.storage.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.data.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * A {@link BasicState} represents a book's list of authors.
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

        /**
         * gets the author's listing essential data
         * @return ID, Author, Book
         */
        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, AUTHOR, BOOK };
        }

        static {
            ID = DataField.makeIDField(AuthorListing.class);
            AUTHOR = DataField.buildFromState(Author.class, "author", Author.Field.ID, FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY);
            BOOK = DataField.buildFromState(Book.class, "book", Book.Field.ID, FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY);
            VALUES = Field.values();
        }
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param id {@inheritDoc}
     * @param builder {@inheritDoc}
     * @see BasicState#BasicState(DataSource, long, StateBuilder)
     */
    AuthorListing(DataSource storage, long id, StateBuilder<AuthorListing> builder) {
        super(storage, id, builder);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param sql {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @see BasicState#BasicState(DataSource, ResultSet)
     */
    public AuthorListing(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param file {@inheritDoc}
     * @see BasicState#BasicState(DataSource, Map)
     */
    public AuthorListing(DataSource storage, Map<String, Object> file) {
        super(storage, file);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataField<Long> getIDField() {
        return Field.ID;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataField<? super Object>[] getFields() {
        return Field.values();
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Type getType() {
        return StateType.AUTHOR_LISTING;
    }

    /**
     * gets the author of the book
     * @return {@link Author}
     */
    public Author getAuthor() {
        return Field.AUTHOR.get(this);
    }

    /**
     * gets the book
     * @return {@link Book}
     */
    public Book getBook() {
        return Field.BOOK.get(this);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public static StateBuilder<AuthorListing> create() {
        return StateBuilder.of(AuthorListing::new, StateType.AUTHOR_LISTING, Field.ID, Field.VALUES);
    }

}
