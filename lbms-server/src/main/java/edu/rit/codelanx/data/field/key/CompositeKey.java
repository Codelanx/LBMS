package edu.rit.codelanx.data.field.key;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.storage.Query;
import edu.rit.codelanx.data.state.State;

import java.util.Arrays;
import java.util.function.UnaryOperator;

public class CompositeKey<T extends State> implements DataKey<T> {

    private final DataField<?>[] fields;

    public CompositeKey(DataField<?>... fields) {
        this.fields = fields;
    }

    @Override
    public <E> void set(T state, DataField<E> field, E value) {
        Query<T> query = state.getLoader().query(state.getType().<T>getConcreteType());
        for (DataField<?> f : fields) {
            Object attempt = f == field ? value : f.get(state);
            query = query.isEqual((DataField<Object>) f, attempt);
        }
        if (query.results().count() > 1) {
            String matching = String.join(",", Arrays.stream(fields).map(DataField::getName).toArray(String[]::new));
            matching = "{" + matching + "}";
            throw new IllegalArgumentException("Cannot update " + field.getName() + ": violates composite key over " + matching);
        }
        field.set(state, value);
    }

    @Override
    public <E> E mutate(T state, DataField<E> field, UnaryOperator<E> operator) {
        Query<T> query = state.getLoader().query(state.getType().<T>getConcreteType());
        E mapped = operator.apply(field.get(state));
        for (DataField<?> f : fields) {
            if (f == field) continue;
            Object value = f.get(state);
            query = query.isEqual((DataField<Object>) f, value);
        }
        if (query.results().count() > 1) {
            String matching = String.join(",", Arrays.stream(fields).map(DataField::getName).toArray(String[]::new));
            matching = "{" + matching + "}";
            throw new IllegalArgumentException("Cannot update field " + field.getName()
                    + " in " + state.getType().getName()
                    + ": violates composite key over " + matching);
        }
        field.set(state, mapped);
        return mapped;
    }
}
