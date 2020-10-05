package edu.rit.codelanx.data.storage.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.storage.field.FieldModifier;

import java.util.function.UnaryOperator;

public class DecoratorImmutable<T> extends FieldDecorator<T> {

    public DecoratorImmutable(DataField<T> parent) {
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
    public FieldModifier getModifierType() {
        return FieldModifier.FM_IMMUTABLE;
    }
}
