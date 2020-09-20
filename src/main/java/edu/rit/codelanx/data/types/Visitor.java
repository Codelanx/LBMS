package edu.rit.codelanx.data.types;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.ResultRow;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StateBuilder;
import edu.rit.codelanx.data.state.UpdatableState;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Visitor extends UpdatableState implements FileSerializable {

    private final WeakReference<Visit> currentVisit = new WeakReference<>(null);
    private final String first;
    private final String last;
    private final String addr;
    private final String phone;
    private final AtomicReference<BigDecimal> money = new AtomicReference<>();

    //Behavioral methods here

    public String getFirstName() {
        return this.first;
    }

    public String getLastName() {
        return this.last;
    }

    public String getAddress() {
        return this.addr;
    }

    public String getPhone() {
        return this.phone;
    }

    public BigDecimal getMoney() {
        return this.money.get();
    }

    @Override
    public State.Type getType() {
        return State.Type.VISITOR;
    }

    BigDecimal updateMoney(BigDecimal amount) {
        this.flagModified();
        return this.money.getAndUpdate(amount::add);
    }

    //Creational handling below

    public static Builder create(DataStorage storage) {
        return new Builder(storage);
    }

    public static class Builder extends StateBuilder<Visitor> {

        private String first;
        private String last;
        private String addr;
        private String phone;
        private double balance;

        private Builder(DataStorage storage) {
            super(storage);
        }

        public Builder firstName(String first) {
            this.first = first;
            return this;
        }

        public Builder lastName(String last) {
            this.last = last;
            return this;
        }

        public Builder address(String addr) {
            this.addr = addr;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder money(double balance) {
            this.balance = balance;
            return this;
        }

        @Override
        public boolean isValid() {
            return this.first != null && this.last != null
                    && this.addr != null && this.phone != null;
        }

        @Override
        public Object[] asSQLArguments() {
            return new Object[] { this.first, this.last, this.addr, this.phone, this.balance };
        }

        @Override
        protected Visitor buildObj(long id) {
            return new Visitor(id, this);
        }
    }

    //Storage serialization handling below

    private Visitor(long id, Builder build) {
        super(id);
        this.first = build.first;
        this.last = build.last;
        this.phone = build.phone;
        this.addr = build.addr;
        this.money.set(BigDecimal.valueOf(build.balance));
    }

    public Visitor(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.first = sql.getString("first");
        this.last = sql.getString("last");
        this.addr = sql.getString("addr");
        this.phone = sql.getString("phone");
    }

    public Visitor(Map<String, Object> file) {
        super((Long) file.get("id"));
        this.first = (String) file.get("first");
        this.last = (String) file.get("last");
        this.addr = (String) file.get("addr");
        this.phone = (String) file.get("phone");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.id);
        back.put("first", this.first);
        back.put("last", this.last);
        back.put("addr", this.addr);
        back.put("phone", this.phone);
        return back;
    }
}
