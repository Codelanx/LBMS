package edu.rit.codelanx.data.storage.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.storage.field.FieldModifier;

public class DecoratorMapping<S extends State, E> extends FieldDecorator<E> {

    public DecoratorMapping(DataField<E> parent) {
        super(parent);
    }

    @Override
    public FieldModifier getModifierType() {
        return null;
    }
}
