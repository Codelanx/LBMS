package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.field.FieldIndicies;
import edu.rit.codelanx.data.storage.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.data.DataSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@StorageContainer("transactions")

/**
 * A {@link BasicState} represents a Transaction between LBMS and the client.
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see BasicState
 */
public class Transaction extends BasicState {

    public static class Field {

        public static final DataField<Long> ID;
        public static final DataField<Visitor> VISITOR;
        public static final DataField<String> REASON;
        public static final DataField<BigDecimal> MONEY;
        private static final DataField<? super Object>[] VALUES;

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, VISITOR, MONEY };
        }

        static {
            ID = DataField.makeIDField(Checkout.class);
            VISITOR = DataField.buildFromState(Visitor.class, "visitor", Visitor.Field.ID, FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY);
            REASON = DataField.buildSimple(String.class, "reason", FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY);
            MONEY = DataField.buildSimple(BigDecimal.class, "money", FieldIndicies.FM_IMMUTABLE);
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
    Transaction(DataSource storage, long id, StateBuilder<Transaction> builder) {
        super(storage, id, builder);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param sql {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @see BasicState#BasicState(DataSource, ResultSet)
     */
    public Transaction(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param file {@inheritDoc}
     * @see BasicState#BasicState(DataSource, Map)
     */
    public Transaction(DataSource storage, Map<String, Object> file) {
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
     * gets the visitor in this transaction
     * @return involved {@link Visitor}
     */
    public Visitor getVisitor() {
        return Field.VISITOR.get(this);
    }

    /**
     * gets the amount of money owned in this transaction
     * @return money of type BigDecimal
     */
    public BigDecimal getAmount() {
        return Field.MONEY.get(this);
    }

    /**
     * gets the reason of this transaction
     * @return string reason
     */
    public String getReason() {
        return Field.REASON.get(this);
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
        return StateType.TRANSACTION;
    }

    /**
     * Façade method for performing a transaction, correctly logging and
     * updating the money
     *
     * @param visitor The {@link Visitor} to update
     * @param amount The amount to <em>add</em> to their balance
     * @param reason A built-in {@link Reason} for consistency
     * @return The new balance for the passed in {@code visitor}
     * @see Visitor#updateMoney(BigDecimal, String)
     */
    public static BigDecimal perform(Visitor visitor, BigDecimal amount, Reason reason) {
        return visitor.updateMoney(amount, reason.getReason());
    }

    /**
     * Façade method for performing a transaction, correctly logging and
     * updating the money
     *
     * @param library The {@link Library} to add to
     * @param visitor The {@link Visitor} to remove from
     * @param amount The amount to <em>add</em> to their balance
     * @param reason A built-in {@link Reason} for consistency
     * @return The new balance for the passed in {@code library}
     * @see Visitor#updateMoney(BigDecimal, String)
     */
    public static BigDecimal perform(Library library, Visitor visitor, BigDecimal amount, Reason reason) {
        visitor.updateMoney(amount.negate(), reason.getReason());
        return library.updateMoney(amount);
    }

    /**
     * build the Transaction State
     * @return {@link StateBuilder} of {@link Transaction}
     */
    public static StateBuilder<Transaction> create() {
        return StateBuilder.of(Transaction::new, StateType.TRANSACTION, Field.ID, Field.VALUES);
    }

    /**
     * enumeration contains reasons for the transaction
     */
    public enum Reason {
        CHARGING_LATE_FEE("late fee"), //library charges a new late fee
        PAYING_LATE_FEE("paying late fee"), //visitor pays late fee balance
        SELLING_BOOK("selling book"), //library sells book to visitor
        PURCHASE_BOOK("purchase book"), //library buys book from book store
        ;

        private final String reason;

        /**
         * establishes the reason for the reason
         * @param reason of type String
         */
        private Reason(String reason) {
            this.reason = reason;
        }

        /**
         * gets the reason
         * @return reason of type String
         */
        public String getReason() {
            return this.reason;
        }
    }

}
