package edu.rit.codelanx.data.cache.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.cache.field.FieldInitializer;
import edu.rit.codelanx.data.cache.field.FieldIndicies;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class FieldIndex<T> implements DataField<T> {

    protected final DataField<T> parent;

    public FieldIndex(DataField<T> parent) {
        this.parent = parent;
    }

    //Returns not the parent, but the original component that was decorated
    protected DataField<T> getComponent() {
        DataField<T> back = this;
        while (back instanceof FieldIndex) {
            back = ((FieldIndex<T>) back).parent;
        }
        return back;
    }

    public abstract FieldIndicies getIndexType();

    @Override
    public boolean isKey() {
        return this.parent.isKey();
    }

    @Override
    public boolean isImmutable() {
        return this.parent.isImmutable();
    }

    @Override
    public boolean hasIndex(FieldIndicies modifier) {
        return this.getIndexType() == modifier || this.parent.hasIndex(modifier);
    }

    @Override
    public T mutate(State state, UnaryOperator<T> updater) {
        return this.parent.mutate(state, updater);
    }

    @Override
    public T get(State state) {
        return this.parent.get(state);
    }

    @Override
    public T set(State state, T value) {
        return this.parent.set(state, value);
    }

    @Override
    public FieldInitializer<T> getInitializer() {
        return this.parent.getInitializer();
    }

    @Override
    public void initialize(State state, Object value) {
        this.parent.initialize(state, value);
    }

    @Override
    public void forget(State state) {
        this.parent.forget(state);
    }

    @Override
    public Object serialize(State state) {
        return this.parent.serialize(state);
    }

    @Override
    public Stream<? extends State> findStatesByValue(T key) {
        return this.parent.findStatesByValue(key);
    }

    @Override
    public boolean isUnique() {
        return this.parent.isUnique();
    }
}
