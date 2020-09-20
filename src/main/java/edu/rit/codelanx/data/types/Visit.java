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

public class Visit extends ResultantState implements FileSerializable {

    private final long visitorID;
    private final Instant start;
    private final Instant end;

    private Visit(long id, Builder builder) {
        super(id);
        this.visitorID = builder.visitorID;
        this.start = builder.start;
        this.end = builder.end;
    }

    public Visit(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.visitorID = sql.getLong("visitor");
        this.start = sql.getTimestamp("start").toInstant();
        this.end = sql.getTimestamp("end").toInstant();
    }

    public Visit(Map<String, Object> file) {
        super((Long) file.get("id"));
        this.visitorID = (Long) file.get("visitor");
        this.start = Instant.ofEpochMilli((Long) file.get("start"));
        this.end = Instant.ofEpochMilli((Long) file.get("end"));
    }

    public Instant getStart() {
        return this.start;
    }

    public Instant getEnd() {
        return this.end;
    }

    @Override
    public State.Type getType() {
        return State.Type.VISIT;
    }

    public static Builder create(DataStorage storage) {
        return new Builder(storage);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.id);
        back.put("start", this.start.toEpochMilli());
        back.put("end", this.end.toEpochMilli());
        return back;
    }

    public static class Builder extends StateBuilder<Visit> {

        private Instant start;
        private Instant end;
        private Long visitorID;

        private Builder(DataStorage storage) {
            super(storage);
        }

        public Builder start(Instant start) {
            this.start = start;
            return this;
        }

        public Builder end(Instant end) {
            this.end = end;
            return this;
        }

        public Builder visitor(Visitor visitor) {
            this.visitorID = visitor.getID();
            return this;
        }

        @Override
        public boolean isValid() {
            return this.start != null && this.end != null && this.visitorID != null;
        }

        @Override
        public Object[] asSQLArguments() {
            return new Object[] {this.visitorID, this.start.toEpochMilli(), this.end.toEpochMilli()};
        }

        @Override
        protected Visit buildObj(long id) {
            return new Visit(id, this);
        }
    }
}
