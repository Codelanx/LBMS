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
/**
 * A {@link BasicState} represents a Book
 * @author sja9291  Spencer Alderman
 * @see BasicState
 */
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

        /**
         * gets the books essential data
         * @return book's id, tittle, isbn, publisher, date, page count, total copies, total checkouts
         */
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
    /** @see BasicState#BasicState(DataStorage, long, StateBuilder)  */
    Book(DataStorage adapter, long id, StateBuilder<Book> builder) {
        super(adapter, id, builder);
    }
    /** @see BasicState#BasicState(DataStorage, ResultSet) */
    public Book(DataStorage storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /**
     *  returns the checkout state of the book
     * @param taker of the book {@link Visitor}
     * @return {@link Checkout} state
     */
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
    /** @see BasicState#BasicState(DataStorage, Map) */
    public Book(DataStorage storage, Map<String, Object> file) {
        super(storage, file);
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
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public String getTitle() {
        return Field.TITLE.get(this);
    }

    /**
     * gets the book's ISBN number
     * @return string ISBN
     */
    public String getISBN() {
        return Field.ISBN.get(this);
    }

    /**
     * gets the book's authors
     * @return stream of {@link Author}
     */
    public Stream<Author> getAuthors() {
        return this.getLoader().query(AuthorListing.class)
                .isEqual(AuthorListing.Field.BOOK, this)
                .results().map(AuthorListing::getAuthor);
    }

    /**
     * get the book's publisher
     * @return string publisher
     */
    public String getPublisher() {
        return Field.PUBLISHER.get(this);
    }

    /**
     * gets the book's publish date
     * @return date of type {@link Instant}
     */
    public Instant getPublishDate() {
        return Field.PUBLISH_DATE.get(this);
    }

    /**
     * gets the book's page count
     * @return int total pages
     */
    public int getPageCount() {
        return Field.PAGE_COUNT.get(this);
    }

    /**
     * gets the book's total copies
     * @return int total copies
     */
    public int getTotalCopies() {
        return Field.TOTAL_COPIES.get(this);
    }

    /**
     * gets number of copies currently being checked out
     * @return int number of copies
     */
    public int getCheckedOut() {
        return Field.CHECKED_OUT.get(this);
    }

    /**
     * gets number of currently available copies of the book
     * @return int number of copies
     */
    public int getAvailableCopies() {
        return this.getTotalCopies() - this.getCheckedOut();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Type getType() {
        return StateType.BOOK;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toFormattedText() {

        return this.getFields().toString();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public static StateBuilder<Book> create() {
        return StateBuilder.of(Book::new, StateType.BOOK, Field.ID, Field.VALUES);
    }

}
