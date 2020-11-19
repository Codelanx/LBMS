package edu.rit.codelanx.data.field.key;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.state.State;

import java.util.function.UnaryOperator;

public interface DataKey<T extends State> {

    default public <E> void set(T state, DataField<E> field, E value) {
        field.set(state, value);
    }

    default public <E> E mutate(T state, DataField<E> field, UnaryOperator<E> operator) {
        return field.mutate(state, operator);
    }
}
