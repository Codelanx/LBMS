package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.cache.StorageContainer;
import edu.rit.codelanx.data.cache.field.DataField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static edu.rit.codelanx.data.cache.field.FieldIndicies.FM_IMMUTABLE;
import static edu.rit.codelanx.data.cache.field.FieldIndicies.FM_KEY;

/**
 * A {@link BasicState} represents a Checkout action
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see BasicState
 */
@StorageContainer("checkouts")
public class Checkout extends BasicState {

    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<Visitor> VISITOR;
        public static final DataField<Book> BOOK;
        public static final DataField<Instant> AT;
        public static final DataField<Boolean> RETURNED;
        private static final DataField<? super Object>[] VALUES;

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, VISITOR, BOOK, AT, RETURNED };
        }

        static {
            ID = DataField.makeIDField(Checkout.class);
            VISITOR = DataField.buildFromState(Visitor.class, "visitor", Visitor.Field.ID, FM_IMMUTABLE, FM_KEY);
            BOOK = DataField.buildFromState(Book.class, "book", Book.Field.ID, FM_IMMUTABLE, FM_KEY);
            AT = DataField.buildSimple(Instant.class, "at", FM_IMMUTABLE);
            RETURNED = DataField.buildSimple(Boolean.class, "returned");
            VALUES = Field.values();
        }
    }
    /** @see BasicState#BasicState(DataSource, long, StateBuilder)  */
    Checkout(DataSource storage, long id, StateBuilder<Checkout> builder) {
        super(storage, id, builder);
    }
    /** @see BasicState#BasicState(DataSource, ResultSet) */
    public Checkout(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }
    /** @see BasicState#BasicState(DataSource, Map) */
    public Checkout(DataSource storage, Map<String, Object> file) {
        super(storage, file);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Checkout.Field.VALUES;
    }

    /**
     * gets the visitor who perform the checkout
     * @return the involved {@link Visitor}
     */
    public Visitor getVisitor() {
        return Checkout.Field.VISITOR.get(this);
    }

    /**
     * gets the books that being checked out
     * @return checkout {@link Book}
     */
    public Book getBook() {
        return Checkout.Field.BOOK.get(this);
    }

    /**
     * gets the checkout time
     * @return time of type {@link Instant}
     */
    public Instant getBorrowedAt() {
        return Checkout.Field.AT.get(this);
    }

    /**
     * gets the time the book was returned
     * @return returning time of type {@link Instant}
     */
    public boolean wasReturned() {
        return Field.RETURNED.get(this);
    }

    /**
     * returns the borrowed Book to the library
     *
     * @return A {@link BigDecimal} describing a fine applied if late, or
     *         {@code null} if no fine was applied
     */
    public BigDecimal returnBook() {
        if (this.wasReturned()) {
            throw new IllegalStateException("Book already returned");
        }
        Duration d = Duration.between(this.getBorrowedAt(), Instant.now());
        //TODO: Determine if a fine should be applied
        if (false) {
            //TODO: And the amount (negative because we're taking from them)
            return Transaction.perform(this.getVisitor(), BigDecimal.valueOf(-1D), Transaction.Reason.CHARGING_LATE_FEE);
        }
        Field.RETURNED.set(this, true);
        return null;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataField<Long> getIDField() {
        return Checkout.Field.ID;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataField<? super Object>[] getFields() {
        return Checkout.Field.values();
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Type getType() {
        return StateType.CHECKOUT;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toFormattedText() {
        String checkout= "Book Checkout: %s| Visitor ID: %d| at: %s| has been returned: %b";
        String formatted_ver= String.format(checkout, this.getBook().getTitle(), this.getVisitor().getID(),
        this.getBorrowedAt().toString(), this.wasReturned());

        return getFields().toString();
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public static StateBuilder<Checkout> create() {
        return StateBuilder.of(Checkout::new, StateType.CHECKOUT, Field.ID, Field.VALUES);
    }

}
