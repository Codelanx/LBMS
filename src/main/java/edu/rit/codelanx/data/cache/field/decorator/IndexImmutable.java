package edu.rit.codelanx.data.cache.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.cache.field.FieldIndicies;

import java.util.function.UnaryOperator;

public class IndexImmutable<T> extends FieldIndex<T> {

    public IndexImmutable(DataField<T> parent) {
        super(parent);
    }

    @Override
    public T set(State state, T value) {
        throw new UnsupportedOperationException("This field is immutable");
    }

    @Override
    public T mutate(State state, UnaryOperator<T> updater) {
        throw new UnsupportedOperationException("This field is immutable");
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public FieldIndicies getIndexType() {
        return FieldIndicies.FM_IMMUTABLE;
    }
}
