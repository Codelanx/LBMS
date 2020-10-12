package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.cache.StorageContainer;
import edu.rit.codelanx.data.cache.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

import static edu.rit.codelanx.data.cache.field.FieldIndicies.*;

/**
 * A {@link BasicState} represents an author
 * @author sja9291  Spencer Alderman
 * @see BasicState
 */
@StorageContainer("authors")
public class Author extends BasicState {

    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<String> NAME;
        private static final DataField<? super Object>[] VALUES;

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, NAME };
        }

        static {
            ID = DataField.makeIDField(Author.class);
            NAME = DataField.buildSimple(String.class, "name", FM_IMMUTABLE, FM_UNIQUE, FM_KEY);
            VALUES = Field.values();
        }
    }
    /** @see BasicState#BasicState(DataSource, long, StateBuilder)  */
    Author(DataSource storage, long id, StateBuilder<Author> author) {
        super(storage, id, author);
    }

    /** @see BasicState#BasicState(DataSource, ResultSet) */
    public Author(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /** @see BasicState#BasicState(DataSource, Map) */
    public Author(DataSource storage, Map<String, Object> file) {
        super(storage, file);
    }

    /**
     * gets the author's name
     * @return string name
     */
    public String getName() {
        return Field.NAME.get(this);
    }

    /**
     * gets the author's books
     * @return stream of books
     */
    public Stream<Book> getBooks() {
        return this.getLoader().query(AuthorListing.class)
                .isEqual(AuthorListing.Field.AUTHOR, this)
                .results().map(AuthorListing::getBook);
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
        return StateType.AUTHOR;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toFormattedText() {

        String author="Author: %s| Author' books: %s";
        String formatted_author= String.format(author, this.getName(), this.getBooks().toString());
        return formatted_author;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public static StateBuilder<Author> create() {
        return StateBuilder.of(Author::new, StateType.AUTHOR, Field.ID, Field.VALUES);
    }

}
