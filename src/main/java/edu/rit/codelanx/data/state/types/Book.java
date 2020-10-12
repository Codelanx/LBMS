package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.storage.StorageContainer;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static edu.rit.codelanx.data.storage.field.FieldIndicies.*;

@StorageContainer("books")
public class Book extends BasicState {

    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<String> TITLE;
        public static final DataField<String> ISBN;
        public static final DataField<String> PUBLISHER;
        public static final DataField<Instant> PUBLISH_DATE;
        public static final DataField<Integer> PAGE_COUNT;
        public static final DataField<Integer> TOTAL_COPIES;
        public static final DataField<Integer> CHECKED_OUT;
        private static final DataField<? super Object>[] VALUES;

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, TITLE, ISBN, PUBLISHER, PUBLISH_DATE, PAGE_COUNT, TOTAL_COPIES, CHECKED_OUT };
        }

        static {
            ID = DataField.makeIDField(Book.class);
            TITLE = DataField.buildSimple(String.class, "title", FM_IMMUTABLE, FM_KEY);
            ISBN = DataField.buildSimple(String.class, "isbn", FM_IMMUTABLE, FM_KEY);
            PUBLISHER = DataField.buildSimple(String.class, "publisher", FM_IMMUTABLE); //TODO: add FM_KEY here?
            PUBLISH_DATE = DataField.buildSimple(Instant.class, "publish_date", FM_IMMUTABLE);
            PAGE_COUNT = DataField.buildSimple(Integer.class, "page_count", FM_IMMUTABLE);
            TOTAL_COPIES = DataField.builder(Integer.class)
                    .name("total_copies")
                    .giveDefaultValue(() -> 1)
                    .build();
            CHECKED_OUT = DataField.builder(Integer.class)
                    .name("checked_out")
                    .giveDefaultValue(() -> 0)
                    .build();
            VALUES = Field.values();
        }
    }

    Book(DataStorage adapter, long id, StateBuilder<Book> builder) {
        super(adapter, id, builder);
    }

    public Book(DataStorage storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    public Checkout checkout(Visitor taker) {
        Field.CHECKED_OUT.mutate(this, old -> {
            if (old <= 0) {
                throw new UnsupportedOperationException("All books are already checked out");
            }
            return old - 1;
        });
        // check out a book
        return Checkout.create()
                .setValue(Checkout.Field.BOOK, this)
                .setValue(Checkout.Field.VISITOR, taker)
                .setValue(Checkout.Field.AT, Instant.now())
                .build(this.getLoader());
    }

    public Book(DataStorage storage, Map<String, Object> file) {
        super(storage, file);
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
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
    }

    public String getTitle() {
        return Field.TITLE.get(this);
    }

    public String getISBN() {
        return Field.ISBN.get(this);
    }

    public Stream<Author> getAuthors() {
        return this.getLoader().query(AuthorListing.class)
                .isEqual(AuthorListing.Field.BOOK, this)
                .results().map(AuthorListing::getAuthor);
    }

    public String getPublisher() {
        return Field.PUBLISHER.get(this);
    }

    public Instant getPublishDate() {
        return Field.PUBLISH_DATE.get(this);
    }

    public int getPageCount() {
        return Field.PAGE_COUNT.get(this);
    }

    public int getTotalCopies() {
        return Field.TOTAL_COPIES.get(this);
    }

    public int getCheckedOut() {
        return Field.CHECKED_OUT.get(this);
    }

    @Override
    public Type getType() {
        return StateType.BOOK;
    }

    @Override
    public String toFormattedText() {

        return this.getFields().toString();
    }

    public static StateBuilder<Book> create() {
        return StateBuilder.of(Book::new, StateType.BOOK, Field.ID, Field.VALUES);
    }

}
