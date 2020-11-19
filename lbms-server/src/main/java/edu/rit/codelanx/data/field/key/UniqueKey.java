package edu.rit.codelanx.data.field.key;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.state.State;

import java.util.function.UnaryOperator;

public class UniqueKey<T extends State> implements DataKey<T> {

    @Override
    public <E> void set(T state, DataField<E> field, E value) {

    }

    @Override
    public <E> E mutate(T state, DataField<E> field, UnaryOperator<E> operator) {
        return null;
    }
}
