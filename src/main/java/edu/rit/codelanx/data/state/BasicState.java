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

public abstract class BasicState implements State, FileSerializable {

    private final AtomicBoolean valid = new AtomicBoolean(false);
    private final DataStorage loader;
    private final Map<DataField<?>, Object> values = new HashMap<>();

    public BasicState(DataStorage loader, long id, StateBuilder<?> builder) {
        this.loader = loader;
        DataField<?> idField = this.getIDField();
        idField.initialize(this, id);
        DataField<?>[] fields = this.getFieldUnsafe(); //id field should be the first indexed field
        for (int i = 0; i < fields.length; i++) { //but just in case we'll iterate them all
            if (fields[i] == idField) continue;
            fields[i].initialize(this, builder.getValue(fields[i]));
        }
    }

    public BasicState(DataStorage loader, Map<String, Object> file) {
        this(loader);
        this.init(loader, f -> InputMapper.getObject(file, f.getName()));
    }

    public BasicState(DataStorage loader, ResultSet sql) throws SQLException {
        this(loader);
        this.initSQL(loader, f -> InputMapper.getObject(f.getType(), sql, f.getName()));
    }

    private BasicState(DataStorage loader) {
        this.loader = loader;
    }

    private void initSQL(DataStorage loader, SQLFunction<DataField<?>, Object> mapper) throws SQLException {
        for (DataField<? super Object> f : this.getFieldUnsafe()) {
            Object o = mapper.apply(f);
            f.initialize(this, InputMapper.toTypeOrState(loader, f.getType(), o));
        }
        this.valid.set(true);
    }

    private void init(DataStorage loader, Function<DataField<?>, Object> mapper) {
        for (DataField<? super Object> f : this.getFieldUnsafe()) {
            Object o = mapper.apply(f);
            f.initialize(this, InputMapper.toTypeOrState(loader, f.getType(), o));
        }
        this.valid.set(true);
    }

    protected abstract DataField<? super Object>[] getFieldUnsafe();

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
        for (DataField<?> f : this.getFieldUnsafe()) {
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
        for (DataField<?> field : this.getFieldUnsafe()) {
            back.put(field.getName(), field.serialize(this));
        }
        return back;
    }

    @Override
    public String toString() {
        return "State{"
                + "id:" + this.getID()
                + ",type:" + this.getType().getNextID()
                + "}";
    }
}
