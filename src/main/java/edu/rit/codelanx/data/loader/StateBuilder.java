package edu.rit.codelanx.data.loader;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.StateType;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class StateBuilder<T extends State> {

    protected final State.Type type;
    private final DataField<?>[] fields;
    private final Map<DataField<?>, Object> values = new HashMap<>(); //always maps DataField<E> -> E
    private final Map<DataField<?>, List<State>> associations = new HashMap<>();

    public StateBuilder(State.Type type, DataField<?>... fields) {
        this.type = type;
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

    public <E extends State> StateBuilder<T> addAssociations(DataField<E> field, E... values) {
        this.associations.computeIfAbsent(field, k -> new ArrayList<>()).addAll(Arrays.asList(values));
        return this;
    }

    public final T build(DataStorage storage) {
        if (!this.isValid()) {
            String missing = Arrays.stream(this.fields)
                    .filter(f -> this.getValue(f) != null)
                    .map(DataField::getName)
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("Cannot build an incomplete object (No fields for: " + missing + ")");
        }
        return storage.insert(this);
    }

    public boolean isValid() {
        return Arrays.stream(this.fields).allMatch(f -> this.getValue(f) != null);
    }

    //TODO: HMMMM I DISLIKE THIS BEING HERE
    public void apply(PreparedStatement stmt) throws SQLException {
        for (int i = 1; i < this.fields.length; i++) {
            stmt.setObject(i, this.getValue(this.fields[i]));
        }
    }

    protected abstract T buildObj(DataStorage storage, long id);

    public static <T extends State> StateBuilder<T> of(StateConstructor<T> constructor, State.Type type, DataField<?>... fields) {
        return new StateBuilder<>(type, fields) {
            @Override
            protected T buildObj(DataStorage storage, long id) {
                return constructor.create(storage, id, this);
            }
        };
    }

    @FunctionalInterface
    public interface StateConstructor<T extends State> {
        public T create(DataStorage storage, long id, StateBuilder<T> builder);
    }

}