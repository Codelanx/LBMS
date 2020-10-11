package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.storage.StorageContainer;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

import static edu.rit.codelanx.data.storage.field.FieldModifier.*;


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

    Author(DataStorage storage, long id, StateBuilder<Author> author) {
        super(storage, id, author);
    }

    public Author(DataStorage storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    public Author(DataStorage storage, Map<String, Object> file) {
        super(storage, file);
    }

    public String getName() {
        return Field.NAME.get(this);
    }

    public Stream<Book> getBooks() {
        return this.getLoader().query(AuthorListing.class)
                .isEqual(AuthorListing.Field.AUTHOR, this)
                .results().map(AuthorListing::getBook);
    }

    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
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
        return StateType.AUTHOR;
    }

    @Override
    public String toFormattedText() {

        String author="Author: %s| Author' books: %s";
        String formatted_author= String.format(author, this.getName(), this.getBooks().toString());
        return formatted_author;
    }

    public static StateBuilder<Author> create() {
        return StateBuilder.of(Author::new, StateType.AUTHOR, Field.ID, Field.VALUES);
    }

}
