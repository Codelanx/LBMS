package edu.rit.codelanx.data.storage;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.field.DataField;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class StateBuilder<T extends State> {

    protected final State.Type type;
    private final DataField<?> idField;
    private final DataField<?>[] fields;
    private final Map<DataField<?>, Object> values = new HashMap<>(); //always maps DataField<E> -> E

    public StateBuilder(State.Type type, DataField<?> idField, DataField<?>... fields) {
        this.type = type;
        this.idField = idField;
        this.fields = fields;
    }

    public State.Type getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked") //map always stores DataField<E> -> E
    public <E> E getValue(DataField<E> field) {
        return (E) this.values.get(field);
    }

    public <E> StateBuilder<T> setValue(DataField<E> field, E value) {
        this.values.put(field, value);
        return this;
    }

    public final T build(DataSource storage) {
        if (!this.isValid()) {
            String missing = Arrays.stream(this.fields)
                    .filter(f -> f != this.idField)
                    .filter(f -> this.getValue(f) == null)
                    .map(DataField::getName)
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("Cannot build an incomplete object (No fields for: " + missing + ")");
        }
        return storage.insert(this);
    }

    public boolean isValid() {
        return Arrays.stream(this.fields)
                .filter(f -> f != this.idField)
                .allMatch(f -> this.getValue(f) != null);
    }

    //REFACTOR: HMMMM I DISLIKE THIS BEING HERE
    public void apply(PreparedStatement stmt) throws SQLException {
        for (int i = 1; i < this.fields.length; i++) {
            stmt.setObject(i, this.getValue(this.fields[i]));
        }
    }

    protected abstract T buildObj(DataSource storage, long id);

    public static <T extends State> StateBuilder<T> of(State.StateBuildConstructor<T> constructor, State.Type type, DataField<Long> idField, DataField<?>... fields) {
        return new StateBuilder<T>(type, idField, fields) {
            @Override
            protected T buildObj(DataSource storage, long id) {
                return constructor.create(storage, id, this);
            }
        };
    }

}
