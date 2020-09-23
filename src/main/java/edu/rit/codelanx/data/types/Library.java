package edu.rit.codelanx.data.types;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.ResultRow;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StateBuilder;
import edu.rit.codelanx.data.state.UpdatableState;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

//There should only ever be one of these initially, but just in case there's multiple...
public class Library extends UpdatableState implements FileSerializable {

    private final AtomicReference<BigDecimal> money = new AtomicReference<>();
    private final AtomicBoolean open = new AtomicBoolean(false);

    public void open() {
        if (!this.open.compareAndSet(false, true)) {
            return; //already open
        }
        //TODO: Anything else?
    }

    public void close() {
        if (!this.open.compareAndSet(true, false)) {
            return; //already closed
        }
        Instant at = Instant.now();
        //TODO: End all visits
    }

    public boolean isOpen() {
        return this.open.get();
    }

    public Library(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.money.set(sql.getBigDecimal("money"));
    }

    public Library(Map<String, Object> file) {
        super((Long) file.get("id"));
        this.money.set(BigDecimal.valueOf((double) file.get("money")));
    }

    public Library(long id, Builder build) {
        super(id);
        this.money.set(build.money);
    }

    public static class Builder extends StateBuilder<Library> {

        private BigDecimal money = BigDecimal.ZERO;

        public Builder(DataStorage storage) {
            super(storage);
        }

        public Builder money(BigDecimal money) {
            this.money = money;
            return this;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Object[] asSQLArguments() {
            return new Object[] { this.money.doubleValue() };
        }

        @Override
        protected Library buildObj(long id) {
            return new Library(id, this);
        }
    }

    @Override
    public long getID() {
        return this.id;
    }

    @Override
    public State.Type getType() {
        return State.Type.LIBRARY;
    }

    @Override
    public Map<String, Object> serialize() {
        return new LinkedHashMap<String, Object>() {{ this.put("money", Library.this.money.get().doubleValue()); }};
    }

    BigDecimal updateMoney(BigDecimal amount) {
        this.flagModified();
        return this.money.getAndUpdate(amount::add);
    }
}
