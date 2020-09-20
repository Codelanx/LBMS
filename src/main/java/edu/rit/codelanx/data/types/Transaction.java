package edu.rit.codelanx.data.types;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.ResultRow;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.ResultantState;
import edu.rit.codelanx.data.state.StateBuilder;
import edu.rit.codelanx.data.state.State;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Transaction extends ResultantState implements FileSerializable {

    //private final long libraryID; //TODO: If we ever go multi-library
    private final long visitorID;
    private final BigDecimal amount; //paid visitor -> library

    private Transaction(long id, Builder builder) {
        super(id);
        this.visitorID = builder.visitorID;
        this.amount = builder.amount;
    }

    public Transaction(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.visitorID = sql.getLong("visitor");
        this.amount = sql.getBigDecimal("amount");
    }

    public Transaction(Map<String, Object> file) {
        super((Long) file.get("id"));
        this.visitorID = (Long) file.get("visitor");
        this.amount = BigDecimal.valueOf((double) file.get("amount"));
    }

    public long getVisitorID() {
        return this.visitorID;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public State.Type getType() {
        return State.Type.TRANSACTION;
    }

    public static Builder create(DataStorage storage) {
        return new Builder(storage);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.id);
        //TODO
        return back;
    }

    public static class Builder extends StateBuilder<Transaction> {

        private Long visitorID;
        private BigDecimal amount; //paid visitor -> library

        private Builder(DataStorage storage) {
            super(storage);
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder visitorID(long visitorID) {
            this.visitorID = visitorID;
            return this;
        }

        @Override
        public boolean isValid() {
            return this.visitorID != null && this.amount != null;
        }

        @Override
        public Object[] asSQLArguments() {
            return new Object[] { this.visitorID, this.amount };
        }

        @Override
        protected Transaction buildObj(long id) {
            return new Transaction(id, this);
        }
    }

}
