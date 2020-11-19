package edu.rit.codelanx.data.field.index;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.field.FieldIndicies;
import edu.rit.codelanx.data.state.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A type of {@link FieldIndex} that represents an unique index object
 *
 * @param <T> specified Type
 * @author sja9291  Spencer Alderman
 */
public class IndexUnique<T> extends FieldIndex<T> {

    private final Map<T, State> uniqueMap = new HashMap<>();

    /**
     * constructs the unique key for an object
     *
     * @param parent {@link DataField} involved
     */
    public IndexUnique(DataField<T> parent) {
        super(parent);
    }

    /**
     * finds the state based on specified value
     *
     * @param key of type {@link T} used to find the state
     * @return {@link Stream} of type {@link State}
     */
    @Override
    public Stream<? extends State> findStatesByValue(T key) {
        return Optional.ofNullable(this.uniqueMap.get(key)).map(Stream::of).orElseGet(Stream::empty);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} since it can be searched via cache
     */
    @Override
    public boolean isKey() {
        return true;
    }

    /**
     * initializes Data field with A specific state and value.
     *
     * @param state {@inheritDoc}
     * @param value {@inheritDoc}
     */
    @Override
    public void initialize(State state, T value) {
        super.initialize(state, value);
        this.uniqueMap.put((T) value, state);
    }

    /**
     * {@inheritDoc}
     *
     * @param state{@inheritDoc}
     */
    @Override
    public void forget(State state) {
        this.uniqueMap.remove(this.get(state));
        super.forget(state);
    }

    /**
     * {@inheritDoc}
     *@return {@code true} since index must be unique
     */
    @Override
    public boolean isUnique() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @param state{@inheritDoc}
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     * @throws IllegalArgumentException if a state found by the key is already exists in map
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@link FieldIndicies#FM_UNIQUE}
     */
    @Override
    public FieldIndicies getIndexType() {
        return FieldIndicies.FM_UNIQUE;
    }
}
