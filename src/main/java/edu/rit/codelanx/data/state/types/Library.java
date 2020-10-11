package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.storage.StorageContainer;
import edu.rit.codelanx.data.storage.field.DataField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static edu.rit.codelanx.data.storage.field.FieldModifier.*;

//There should only ever be one of these initially, but just in case there's multiple...
@StorageContainer("libraries")
public class Library extends BasicState {

    private final AtomicBoolean open = new AtomicBoolean(false);

    Library(DataStorage loader, long id, StateBuilder<Library> builder) {
        super(loader, id, builder);
    }

    public Library(DataStorage loader, ResultSet sql) throws SQLException {
        super(loader, sql);
    }

    public Library(DataStorage loader, Map<String, Object> file) {
        super(loader, file);
    }

    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<BigDecimal> MONEY;
        private static final DataField<? super Object>[] VALUES;

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, MONEY };
        }

        static {
            ID = DataField.makeIDField(Library.class);
            MONEY = DataField.buildSimple(BigDecimal.class, "money", FM_IMMUTABLE, FM_UNIQUE, FM_KEY);
            VALUES = Field.values();
        }
    }

    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
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
        return StateType.LIBRARY;
    }

    @Override
    public String toFormattedText() {
        String lib="Library ID: %s| Is Open: %b";
        String formatted_lib=String.format(lib, this.getIDField(), this.isOpen());
        return formatted_lib;
    }

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
        this.getLoader().getRelativeStorage()
                .getStateStorage(Visitor.class)
                .forAllLoaded(vis -> vis.endVisit(at));
    }

    public void transact(Visitor visitor, BigDecimal amount, String reason) {
        visitor.updateMoney(amount, reason);
        Field.MONEY.mutate(this, amount::add);
    }

    public boolean isOpen() {
        return this.open.get();
    }

    BigDecimal updateMoney(BigDecimal amount) {
        return Field.MONEY.set(this, amount);
    }

    public static Builder create() {
        return new Builder();
        //TODO: replace with below once command code is fixed
        //return StateBuilder.of(Library::new, StateType.LIBRARY, Field.VALUES);
    }

    public static class Builder extends StateBuilder<Library> {

        public Builder() {
            super( StateType.LIBRARY, Field.ID, Field.VALUES);
        }

        @Deprecated
        public Builder money(BigDecimal money) {
            this.setValue(Field.MONEY, money);
            return this;
        }

        @Override
        protected Library buildObj(DataStorage storage, long id) {
            return new Library(storage, id, this);
        }

    }

}
