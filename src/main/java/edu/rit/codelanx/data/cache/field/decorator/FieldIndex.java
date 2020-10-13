package edu.rit.codelanx.data.cache.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.cache.field.FieldInitializer;
import edu.rit.codelanx.data.cache.field.FieldIndicies;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * a {@link DataField} represent the field index
 *
 * @param <T> of specified type
 * @author sja9291  Spencer Alderman
 */
public abstract class FieldIndex<T> implements DataField<T> {

    protected final DataField<T> parent;

    /**
     * constructs the field index
     *
     * @param parent of type {@link DataField}
     */
    public FieldIndex(DataField<T> parent) {
        this.parent = parent;
    }

    /**
     * gets the original component component that was decorated
     *
     * @return component of type {@link DataField}
     */
    //Returns not the parent, but the original component that was decorated
    protected DataField<T> getComponent() {
        DataField<T> back = this;
        while (back instanceof FieldIndex) {
            back = ((FieldIndex<T>) back).parent;
        }
        return back;
    }

    /**
     * gets the type of the index
     *
     * @return {@link FieldIndex} type
     */
    public abstract FieldIndicies getIndexType();

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isKey() {
        return this.parent.isKey();
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isImmutable() {
        return this.parent.isImmutable();
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean hasIndex(FieldIndicies modifier) {
        return this.getIndexType() == modifier || this.parent.hasIndex(modifier);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public T mutate(State state, UnaryOperator<T> updater) {
        return this.parent.mutate(state, updater);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public T get(State state) {
        return this.parent.get(state);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public T set(State state, T value) {
        return this.parent.set(state, value);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public FieldInitializer<T> getInitializer() {
        return this.parent.getInitializer();
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public void initialize(State state, Object value) {
        this.parent.initialize(state, value);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public void forget(State state) {
        this.parent.forget(state);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object serialize(State state) {
        return this.parent.serialize(state);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Stream<? extends State> findStatesByValue(T key) {
        return this.parent.findStatesByValue(key);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isUnique() {
        return this.parent.isUnique();
    }
}
