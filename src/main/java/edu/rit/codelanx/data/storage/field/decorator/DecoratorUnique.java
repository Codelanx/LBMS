package edu.rit.codelanx.data.storage.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.storage.field.FieldModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class DecoratorUnique<T> extends FieldDecorator<T> {

    private final Map<T, State> uniqueMap = new HashMap<>();

    public DecoratorUnique(DataField<T> parent) {
        super(parent);
    }

    @Override
    public Stream<? extends State> findStatesByValue(T key) {
        //In JDK 9+, Optional#stream would be pretty nice here, hence the warning below
        return Optional.ofNullable(this.uniqueMap.get(key)).map(Stream::of).orElseGet(Stream::empty);
    }

    @Override
    public boolean isKey() {
        return true;
    }

    @Override
    public void initialize(State state, Object value) {
        super.initialize(state, value);
        this.uniqueMap.put((T) value, state);
    }

    @Override
    public void forget(State state) {
        this.uniqueMap.remove(this.get(state));
        super.forget(state);
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public T set(State state, T key) {
        State curr = this.uniqueMap.get(key);
        if (curr != null) {
            throw new IllegalArgumentException("Cannot set new value, state already exists with it (" + curr + ")");
        }
        T old = super.set(state, key);
        this.uniqueMap.remove(old);
        this.uniqueMap.put(key, state);
        return old;
    }

    @Override
    public FieldModifier getModifierType() {
        return FieldModifier.FM_UNIQUE;
    }
}
