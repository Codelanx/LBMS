package edu.rit.codelanx.data.storage.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.storage.field.FieldModifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class DecoratorKey<T> extends FieldDecorator<T> {

    private final Map<Object, Set<State>> states = new HashMap<>();

    public DecoratorKey(DataField<T> parent) {
        super(parent);
    }

    @Override
    public void initialize(State state, Object value) {
        super.initialize(state, value);
        this.getStates((T) value).add(state);
    }

    @Override
    public Stream<? extends State> findStatesByValue(T key) {
        return Optional.ofNullable(this.states.get(key)).map(Set::stream).orElse(Stream.empty());
    }

    private Set<State> getStates(T value) {
        return this.states.computeIfAbsent(value, k -> new HashSet<>());
    }

    @Override
    public T set(State state, T value) {
        T old = super.set(state, value);
        this.states.computeIfPresent(old, (k, set) -> {
            set.remove(state);
            return set;
        });
        this.getStates(value).add(state);
        return old;
    }

    @Override
    public void forget(State state) {
        this.states.compute(this.get(state), (k, old) -> {
            return old != null && old.remove(state) && old.isEmpty()
                    ? null
                    : old;
        });
        super.forget(state);
    }

    @Override
    public boolean isKey() {
        return true;
    }

    @Override
    public FieldModifier getModifierType() {
        return FieldModifier.FM_KEY;
    }
}
