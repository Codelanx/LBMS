package edu.rit.codelanx.data.cache.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.cache.field.FieldIndicies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * a {@link FieldIndex} used for object lookup.
 * @param <T> of type {@link FieldIndex}
 */
public class IndexKey<T> extends FieldIndex<T> {

    private final Map<Object, Set<State>> states = new HashMap<>();

    /**
     * constructs the index key for an object
     * @param parent {@link DataField} to be indexed
     */
    public IndexKey(DataField<T> parent) {
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
    /**
     * {@inheritDoc}
     * @return {@code true} since it's the index key
     */
    @Override
    public boolean isKey() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@link IndexKey} type
     */
    @Override
    public FieldIndicies getIndexType() {
        return FieldIndicies.FM_KEY;
    }
}
