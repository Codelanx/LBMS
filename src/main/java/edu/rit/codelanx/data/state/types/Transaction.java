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

import static edu.rit.codelanx.data.storage.field.FieldModifier.*;

@StorageContainer("transactions")
/**
 * A {@link BasicState} represents a Transaction between LBMS and the client.
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
            REASON = DataField.buildSimple(String.class, "reason", FM_IMMUTABLE);
            MONEY = DataField.buildSimple(BigDecimal.class, "money", FM_IMMUTABLE);
            VALUES = Library.Field.values();
        }
    }

    Transaction(DataStorage storage, long id, StateBuilder<Transaction> builder) {
        super(storage, id, builder);
    }

    public Transaction(DataStorage storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    public Transaction(DataStorage storage, Map<String, Object> file) {
        super(storage, file);
    }

    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
    }

    public Visitor getVisitor() {
        return Field.VISITOR.get(this);
    }

    public BigDecimal getAmount() {
        return Field.MONEY.get(this);
    }

    public String getReason() {
        return Field.REASON.get(this);
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
        return StateType.TRANSACTION;
    }

    @Override
    public String toFormattedText() {
        String transaction="Transaction ID: %d| Visitor: %s %s| Reason: %s| Amount: %s";
        String formatted_transaction = String.format(transaction, this.getID(), this.getVisitor().getFirstName(),
                this.getVisitor().getLastName(), this.getReason(), this.getAmount().toPlainString() );
        return formatted_transaction;
    }

    public static Builder create() {
        return new Builder();
        //TODO: replace with below once command code is fixed
        //return StateBuilder.of(Transaction::new, StateType.TRANSACTION, Field.VALUES);
    }

    @Deprecated
    public static class Builder extends StateBuilder<Transaction> {

        private Builder() {
            super(StateType.TRANSACTION, Field.ID, Field.VALUES);
        }

        @Deprecated
        public Builder amount(BigDecimal amount) {
            this.setValue(Field.MONEY, amount);
            return this;
        }

        @Deprecated
        public Builder visitorID(Visitor visitor) {
            this.setValue(Field.VISITOR, visitor);
            return this;
        }

        @Override
        protected Transaction buildObj(DataStorage storage, long id) {
            return new Transaction(storage, id, this);
        }
    }

}
