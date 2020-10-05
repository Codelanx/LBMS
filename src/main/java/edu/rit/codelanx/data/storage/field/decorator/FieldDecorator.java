package edu.rit.codelanx.data.storage.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.storage.field.FieldInitializer;
import edu.rit.codelanx.data.storage.field.FieldModifier;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class FieldDecorator<T> implements DataField<T> {

    protected final DataField<T> parent;

    public FieldDecorator(DataField<T> parent) {
        this.parent = parent;
    }

    //Returns not the parent, but the original component that was decorated
    protected DataField<T> getComponent() {
        DataField<T> back = this;
        while (back instanceof FieldDecorator) {
            back = ((FieldDecorator<T>) back).parent;
        }
        return back;
    }

    public abstract FieldModifier getModifierType();

    @Override
    public boolean isKey() {
        return this.parent.isKey();
    }

    @Override
    public boolean isImmutable() {
        return this.parent.isImmutable();
    }

    @Override
    public boolean hasModifier(FieldModifier modifier) {
        return this.getModifierType() == modifier || this.parent.hasModifier(modifier);
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
