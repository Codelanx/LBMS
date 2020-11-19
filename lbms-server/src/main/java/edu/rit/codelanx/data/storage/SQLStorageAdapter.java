package edu.rit.codelanx.data.storage;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.util.Errors;
import com.codelanx.commons.data.ResultRow;
import com.codelanx.commons.data.SQLDataType;
import com.codelanx.commons.data.SQLResponse;
import com.codelanx.commons.util.cache.Cache;
import edu.rit.codelanx.ConfigKey;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.types.StateType;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.field.DataField;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A type of {@link StorageAdapter} for sql database.
 *
 * @author sja9291  Spencer Alderman
 */
public class SQLStorageAdapter implements StorageAdapter {

    private final Cache<? extends SQLDataType> db;
    private final Library lib;
    private final DataSource storage;

    /**
     * Creates sql adapter for
     * @param storage {@link DataSource}
     */
    public SQLStorageAdapter(DataSource storage) {
        this.storage = storage;
        this.db = ConfigKey.newDBCache();
        SQLResponse<Library> resp = this.db.get().query(rs -> {
            if (rs.next()) { //select the first available result, since we're not multi-library
                return new Library(storage, rs);
            }
            return null;
        }, StatementType.SELECT_ALL.forType(Library.class));
        if (resp.getException() != null) {
            Errors.reportAndExit(resp.getException());
        }
        Library lib = resp.getResponse();
        if (lib == null) {
            //No library yet, make one!
            lib = Library.create()
                    .setValue(Library.Field.MONEY, BigDecimal.ZERO)
                    .build(storage);
        }
        this.lib = lib;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Library getLibrary() {
        return this.lib;
    }

    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        String stmt = StatementType.CREATE_OBJECT.forType(builder.getType().getConcreteType());
        try {
            PreparedStatement psql = this.db.get().getConnection().prepareStatement(stmt);
            builder.apply(psql);
            int done = psql.executeUpdate();
            if (done <= 0) {
                throw new RuntimeException("SQL failed to insert, no object created");
            }
            //TODO: We'll pray this is good enough for now
            long id = this.db.get().selectFirst(ResultRow::getLong, "SELECT LAST_INSERT_ID()").getResponse();
            return builder.buildObj(this.storage, id);
        } catch (SQLException ex) {
            Errors.reportAndExit(ex);
        }
        return null; //should not reach
    }

    /**
     * Does nothing, as SQL is loaded on an as-needed basis
     */
    @Override
    public void loadAll() {
        //no preloading with sql, simply utilizes the backend
    }
    /**
     * Does nothing, as any updates will have already been committed to the
     * remote data source
     */
    @Override
    public void saveAll() throws IOException {
        //no saving needed, updates are pushed as made
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataSource getAdaptee() {
        return this.storage;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State> R loadState(long id, Class<R> type) {
        State.Type stateType = StateType.fromClass(type);
        return this.db.get().query(rs -> {
            if (rs.next()) {
                return InputMapper.toState(storage, type, id);
            }
            return null;
        }, StatementType.FIND_BY_ID.forType(type), id).getResponse();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State> Stream<R> handleQuery(StateQuery<R> query) {
        //TODO: Attempt the local cache before resorting to SQL?
        State.Type type = StateType.fromClass(query.getType());
        try {
            return query.runSQLQuery((stmt, args) -> this.db.get().query(rs -> {
                List<R> back = new ArrayList<>();
                while (rs.next()) {
                    back.add(type.<R>getSQLConstructor().create(storage, rs));
                }
                return back.stream();
            }, stmt, args).getResponse());
        } catch (SQLException ex) {
            Errors.report(ex); //"Silent failure"
        }
        return Stream.empty();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State, E> Stream<R> loadState(Class<R> type, DataField<E> field, E value) {
        return this.storage.query(type)
                .isEqual(field, value)
                .results();
    }

    /**
     * {@inheritDoc}
     * @param state {@inheritDoc}
     * @param field {@inheritDoc}
     * @param value {@inheritDoc}
     */
    @Override
    public <E> void notifyUpdate(State state, DataField<E> field, E value) {
        //TODO: update db
    }
    /**
     * {@inheritDoc}
     * @param state {@inheritDoc}
     */
    @Override
    public void remove(State state) {
        //TODO: update db
    }
    /**
     * {@inheritDoc}
     * @return {@code false}, SQL offloads data from memory
     */
    @Override
    public boolean isCached() {
        return false;
    }

    private enum StatementType {
        CREATE_OBJECT,
        CREATE_CONTAINER,
        FIND_BY_ID,
        SELECT_ALL,
        ;

        public String forType(Class<? extends State> type) {
            StorageContainer container = type.getAnnotation(StorageContainer.class);
            if (container == null) {
                throw new IllegalArgumentException("No @StorageContainer defined for state: " + type.getSimpleName());
            }
            switch (this) {
                case SELECT_ALL:
                    return "SELECT * FROM " + container.value();
                case FIND_BY_ID:
                    return "SELECT * FROM " + container.value() + " WHERE id = ?";
                case CREATE_OBJECT:
                    //TODO: Load .sql files from resources
                case CREATE_CONTAINER:
                    //TODO: save .sql files from heidisql and load from resources
            }
            throw new IllegalArgumentException("Cannot map " + type.getSimpleName() + " to a " + this.name()
                    + " statement (not implemented yet?)");
        }
    }

}
