package edu.rit.codelanx.data.types;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.ResultRow;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.ResultantState;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StateBuilder;

import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class Checkout extends ResultantState implements FileSerializable {

    //private final long libraryID; //TODO: If we ever go multi-library
    private final long visitorID;
    private final long bookID;
    private final Instant at;

    private Checkout(long id, Builder builder) {
        super(id);
        this.visitorID = builder.visitorID;
        this.bookID = builder.bookID;
        this.at = builder.at;
    }

    public Checkout(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.visitorID = sql.getLong("visitor");
        this.bookID = sql.getLong("book");
        this.at = sql.getTimestamp("at").toInstant();
    }

    public Checkout(Map<String, Object> file) {
        super((Long) file.get("id"));
        this.visitorID = (Long) file.get("visitor");
        this.bookID = (Long) file.get("book");
        this.at = Instant.ofEpochMilli((Long) file.get("at"));
    }

    public long getVisitorID() {
        return this.visitorID;
    }

    public long getBookID() {
        return this.bookID;
    }

    public Instant getBorrowedAt() {
        return this.at;
    }

    @Override
    public State.Type getType() {
        return State.Type.CHECKOUT;
    }

    @Override
    public String toFormattedText() {
        return null;
    }

    public static Builder create(DataStorage storage) {
        return new Builder(storage);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.id);
        back.put("visitor", this.visitorID);
        back.put("book", this.bookID);
        back.put("at", this.at.toEpochMilli());
        return back;
    }

    public static class Builder extends StateBuilder<Checkout> {

        private Long visitorID;
        private Long bookID;
        private Instant at;

        private Builder(DataStorage storage) {
            super(storage);
        }

        public Builder at(Instant at) {
            this.at = at;
            return this;
        }

        public Builder bookID(long bookID) {
            this.bookID = bookID;
            return this;
        }

        public Builder visitorID(long visitorID) {
            this.visitorID = visitorID;
            return this;
        }

        @Override
        public boolean isValid() {
            return this.visitorID != null && this.bookID != null && this.at != null;
        }

        @Override
        public Object[] asSQLArguments() {
            return new Object[]{this.visitorID, this.bookID, this.at.toEpochMilli()};
        }

        @Override
        protected Checkout buildObj(long id) {
            return new Checkout(id, this);
        }
    }

}
