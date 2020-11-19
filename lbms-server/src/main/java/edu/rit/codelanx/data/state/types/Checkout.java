package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.field.FieldIndicies;
import edu.rit.codelanx.data.storage.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.data.DataSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * A {@link BasicState} represents a Checkout action
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see BasicState
 */
@StorageContainer("checkouts")
public class Checkout extends BasicState {

    public static final long BORROW_DAYS = 7; //number of days you can borrow
    private static final BigDecimal INITIAL_FINE = BigDecimal.valueOf(10);
    private static final BigDecimal WEEKLY_FINE = BigDecimal.valueOf(2);
    private static final BigDecimal MAX_FINE = BigDecimal.valueOf(30);

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
            VISITOR = DataField.buildFromState(Visitor.class, "visitor", Visitor.Field.ID, FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY);
            BOOK = DataField.buildFromState(Book.class, "book", Book.Field.ID, FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY);
            AT = DataField.buildSimple(Instant.class, "at");
            RETURNED = DataField.buildSimple(Boolean.class, "returned");
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
    Checkout(DataSource storage, long id, StateBuilder<Checkout> builder) {
        super(storage, id, builder);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param sql {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @see BasicState#BasicState(DataSource, ResultSet)
     */
    public Checkout(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param file {@inheritDoc}
     * @see BasicState#BasicState(DataSource, Map)
     */
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
     * @param clock The {@link Clock} to reference for the time
     * @return A {@link BigDecimal} describing a fine applied if late, or
     *         {@code null} if no fine was applied
     */
    public BigDecimal returnBook(Clock clock) {
        if (this.wasReturned()) {
            throw new IllegalStateException("Book already returned");
        }
        this.getBook().addCopy(1);
        Duration d = Duration.between(this.getBorrowedAt(), clock.getCurrentTime());
        //Due 7 days after checkout (if checked out on monday, not late until next tuesday)
        //Initial Late fee - $10 ($10 owed that tuesday)
        //+$2/week         -     ($12 by the next tuesday)
        //max fine: $30    -     ($30 after 10 weeks late)
        long days = Duration.between(this.getBorrowedAt(), clock.getCurrentTime()).toDays();
        long weeksLate = days / 7;
        if (weeksLate > 0) {
            BigDecimal amount = INITIAL_FINE;
            amount = amount.add(WEEKLY_FINE.multiply(BigDecimal.valueOf(weeksLate - 1)));
            amount = amount.min(MAX_FINE);
            this.getLoader().getLibrary().updateMoney(amount);
            return Transaction.perform(this.getVisitor(), amount.negate(), Transaction.Reason.CHARGING_LATE_FEE);
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
    public static StateBuilder<Checkout> create() {
        return StateBuilder.of(Checkout::new, StateType.CHECKOUT, Field.ID, Field.VALUES);
    }

}
