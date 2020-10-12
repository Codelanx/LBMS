package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.storage.StorageContainer;
import edu.rit.codelanx.data.storage.field.DataField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static edu.rit.codelanx.data.storage.field.FieldIndicies.*;

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
            VISITOR = DataField.buildFromState(Visitor.class, "visitor", Visitor.Field.ID, FM_IMMUTABLE, FM_KEY);
            REASON = DataField.buildSimple(String.class, "reason", FM_IMMUTABLE, FM_KEY);
            MONEY = DataField.buildSimple(BigDecimal.class, "money", FM_IMMUTABLE);
            VALUES = Field.values();
        }
    }
    /** @see BasicState#BasicState(DataStorage, long, StateBuilder)  */
    Transaction(DataStorage storage, long id, StateBuilder<Transaction> builder) {
        super(storage, id, builder);
    }
    /** @see BasicState#BasicState(DataStorage, ResultSet) */
    public Transaction(DataStorage storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }
    /** @see BasicState#BasicState(DataStorage, Map) */
    public Transaction(DataStorage storage, Map<String, Object> file) {
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
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toFormattedText() {
        String transaction="Transaction ID: %d| Visitor: %s %s| Reason: %s| Amount: %s";
        String formatted_transaction = String.format(transaction, this.getID(), this.getVisitor().getFirstName(),
                this.getVisitor().getLastName(), this.getReason(), this.getAmount().toPlainString() );
        return formatted_transaction;
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
