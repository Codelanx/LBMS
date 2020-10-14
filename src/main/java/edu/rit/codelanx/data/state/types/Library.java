package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.LBMS;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.util.Clock;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static edu.rit.codelanx.data.cache.field.FieldIndicies.*;

//There should only ever be one of these initially, but just in case there's multiple...
/**
 * A {@link BasicState} represents a Library
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see BasicState
 */
@StorageContainer("libraries")
public class Library extends BasicState {

    //If the library is open to run commands / transactions / etc
    private final AtomicBoolean open = new AtomicBoolean(false);
    /** @see BasicState#BasicState(DataSource, long, StateBuilder)  */
    Library(DataSource loader, long id, StateBuilder<Library> builder) {
        super(loader, id, builder);
    }

    /** @see BasicState#BasicState(DataSource, ResultSet) */
    public Library(DataSource loader, ResultSet sql) throws SQLException {
        super(loader, sql);
    }
    /** @see BasicState#BasicState(DataSource, Map) */
    public Library(DataSource loader, Map<String, Object> file) {
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
            MONEY = DataField.buildSimple(BigDecimal.class, "money", FM_UNIQUE, FM_KEY);
            VALUES = Field.values();
        }
    }

    private volatile Clock clock;

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
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
        return StateType.LIBRARY;
    }

    /**
     * opens the library so visitors can make visits
     */
    public void open() {
        if (!this.open.compareAndSet(false, true)) {
            return; //already open
        }
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * closes the library, stops all visits
     */
    public void close() {
        if (!this.open.compareAndSet(true, false)) {
            return; //already closed
        }
        Instant at = this.clock.getCurrentTime();
        this.getLoader().getRelativeStorage()
                .getStateStorage(Visitor.class)
                .forAllLoaded(vis -> vis.endVisit(at));
    }

    public Clock getClock() {
        return this.clock;
    }

    public boolean isOpen() {
        if (LBMS.PREPRODUCTION_DEBUG) {
            return true;
        }
        return this.open.get();
    }

    //adds the given amount to the current balance
    BigDecimal updateMoney(BigDecimal amount) {
        return Field.MONEY.mutate(this, amount::add);
    }

    public static StateBuilder<Library> create() {
        return StateBuilder.of(Library::new, StateType.LIBRARY, Field.ID, Field.VALUES);
    }
}
