package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.field.FieldIndicies;
import edu.rit.codelanx.data.storage.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.util.Clock;
import edu.rit.codelanx.data.DataSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

//There should only ever be one of these initially, but just in case there's multiple...
/**
 * A {@link BasicState} represents a Library
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see BasicState
 */
@StorageContainer("libraries")
public class Library extends BasicState {

    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<BigDecimal> MONEY;
        private static final DataField<? super Object>[] VALUES;

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, MONEY };
        }

        static {
            ID = DataField.makeIDField(Library.class);
            MONEY = DataField.buildSimple(BigDecimal.class, "money", FieldIndicies.FM_UNIQUE, FieldIndicies.FM_KEY);
            VALUES = Field.values();
        }
    }

    //If the library is open to run commands / transactions / etc
    private final AtomicBoolean open = new AtomicBoolean(false);
    private volatile Clock clock;

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param id {@inheritDoc}
     * @param builder {@inheritDoc}
     * @see BasicState#BasicState(DataSource, long, StateBuilder)
     */
    Library(DataSource storage, long id, StateBuilder<Library> builder) {
        super(storage, id, builder);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param sql {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @see BasicState#BasicState(DataSource, ResultSet)
     */
    public Library(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param file {@inheritDoc}
     * @see BasicState#BasicState(DataSource, Map)
     */
    public Library(DataSource storage, Map<String, Object> file) {
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
