package edu.rit.codelanx.data.cache.field.decorator;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.cache.field.FieldInitializer;
import edu.rit.codelanx.data.state.State;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

//holds references to individual data fields, in order to allow 1 field per
//datasource
public class DataFieldSource<T> implements DataField<T> {

    private final Map<DataSource, DataField<T>> fields = new WeakHashMap<>();
    private final FieldInitializer<T> init;
    private final Supplier<? extends DataField<T>> builder;

    public DataFieldSource(FieldInitializer<T> init, Supplier<? extends DataField<T>> builder) {
        this.init = init;
        this.builder = builder;
    }

    private DataField<T> getField(State state) {
        return this.getField(state.getLoader());
    }

    private DataField<T> getField(DataSource source) {
        return this.fields.computeIfAbsent(source, load -> builder.get());
    }

    @Override
    public Object serialize(State state) {
        return this.getField(state).serialize(state);
    }

    @Override
    public void initialize(State state, Object value) {
        this.getField(state).initialize(state, value);
    }

    @Override
    public void forget(State state) {
        this.getField(state).forget(state);
    }

    @Override
    public T mutate(State state, UnaryOperator<T> updater) {
        return this.getField(state).mutate(state, updater);
    }

    @Override
    public T get(State state) {
        return this.getField(state).get(state);
    }

    @Override
    public T set(State state, T value) {
        return this.getField(state).set(state, value);
    }

    @Override
    public FieldInitializer<T> getInitializer() {
        return this.init;
    }

    @Override
    public Stream<? extends State> findStatesByValue(DataSource source, T key) {
        DataField<T> back = this.fields.get(source);
        if (back == null) {
            return Stream.empty(); //no data loaded for this source
        }
        return back == null
                ? Stream.empty()
                : back.findStatesByValue(key);
    }

    @Override
    public Stream<? extends State> findStatesByValue(T key) {
        throw new UnsupportedOperationException("Must specify a DataSource to find states on");
    }

}
