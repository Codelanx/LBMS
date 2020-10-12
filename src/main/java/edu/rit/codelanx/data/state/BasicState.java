package edu.rit.codelanx.data.state;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.SQLFunction;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.loader.InputMapper;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * A skeleton implementation for {@link State} which classes can extend from
 *
 * @see State
 * @author sja9291  Spencer Alderman
 */
public abstract class BasicState implements State, FileSerializable {

    private final AtomicBoolean valid = new AtomicBoolean(false);
    private final long id;
    private final DataStorage loader;
    private final Map<DataField<?>, Object> values = new HashMap<>();

    /**
     * construct the state from State buil
     * @param loader {@link DataStorage}
     * @param id long type
     * @param builder {@link StateBuilder}
     */
    public BasicState(DataStorage loader, long id, StateBuilder<?> builder) {
        this.loader = loader;
        this.id = id;
        DataField<?> idField = this.getIDField();
        idField.initialize(this, id);
        //id field should be the first indexed field
        DataField<?>[] fields = this.getFieldsUnsafe();
        //but just in case we'll iterate them all
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == idField) continue; //skip id field
            fields[i].initialize(this, builder.getValue(fields[i]));
        }
    }

    /**
     * constructs the state from file input.
     * @param loader {@link DataStorage}
     * @param file- to get input from
     */
    public BasicState(DataStorage loader, Map<String, Object> file) {
        this.loader = loader;
        this.id = this.init(loader, f -> InputMapper.getObject(file, f.getName()));
    }

    /**
     * takes input from sql database and constructs the state
     * @param loader {@link DataStorage}
     * @param sql {@link ResultSet}
     * @throws SQLException when errors occur
     */
    public BasicState(DataStorage loader, ResultSet sql) throws SQLException {
        this.loader = loader;
        this.id = this.initSQL(loader, f -> InputMapper.getObject(f.getType(), sql, f.getName()));
    }

    //helper method to initialize the id/data fields
    private long initSQL(DataStorage loader, SQLFunction<DataField<?>, Object> mapper) throws SQLException {
        long id = InputMapper.toType(Long.class, mapper.apply(this.getIDField()));
        for (DataField<? super Object> f : this.getFieldsUnsafe()) {
            if (f == (DataField<?>) this.getIDField()) continue;
            Object o = mapper.apply(f);
            f.initialize(this, InputMapper.toTypeOrState(loader, f.getType(), o));
        }
        this.valid.set(true);
        return id;
    }

    //same as #initSQL without the SQLException
    private long init(DataStorage loader, Function<DataField<?>, Object> mapper) {
        try {
            return this.initSQL(loader, mapper::apply);
        } catch (SQLException ex) { //Should never happen - mapper::apply does not produce SQLException
            throw new RuntimeException("Failed to initialize state", ex);
        }
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public long getID() {
        return this.id;
    }

    protected abstract DataField<? super Object>[] getFieldsUnsafe();
    
    @Override
    public DataStorage getLoader() {
        return this.loader;
    }

    @Override
    public boolean isValid() {
        return this.valid.get();
    }

    @Override
    public void unload() {
        this.valid.set(false);
        this.values.clear();
        this.getIDField().forget(this);
        for (DataField<?> f : this.getFieldsUnsafe()) {
            f.forget(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicState)) return false;
        BasicState that = (BasicState) o;
        return this.getID() == that.getID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getID());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.getID());
        for (DataField<?> field : this.getFieldsUnsafe()) {
            back.put(field.getName(), field.serialize(this));
        }
        return back;
    }

    /**
     * string representation of the state
     * @return id, state type string
     */
    @Override
    public String toString() {
        return "State{"
                + "id:" + this.getID()
                + ",type:" + this.getType().getNextID()
                + "}";
    }
}
