package edu.rit.codelanx.data.cache.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.cache.field.FieldIndicies;

import java.util.function.UnaryOperator;

/**
 * A type of {@link FieldIndex} that represents an immutable index object
 *
 * @param <T> specified Type
 * @author sja9291  Spencer Alderman
 */
public class IndexImmutable<T> extends FieldIndex<T> {

    /**
     * instantiates a data field that should not be mutable
     *
     * @param parent component {@link DataField} to be decorated by
     */
    public IndexImmutable(DataField<T> parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     *
     * @param state {@inheritDoc}
     * @param value {@inheritDoc}
     * @return UnsupportedOperationException since the field cannot be mutated
     */
    @Override
    public T set(State state, T value) {
        throw new UnsupportedOperationException("This field is immutable");
    }

    /**
     * {@inheritDoc}
     *
     * @param state{@inheritDoc}
     * @param updater{@inheritDoc}
     * @return UnsupportedOperationException since the field immutable
     */
    @Override
    public T mutate(State state, UnaryOperator<T> updater) {
        throw new UnsupportedOperationException("This field is immutable");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} since this's immutable
     */
    @Override
    public boolean isImmutable() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@link FieldIndicies#FM_IMMUTABLE}
     */
    @Override
    public FieldIndicies getIndexType() {
        return FieldIndicies.FM_IMMUTABLE;
    }
}
