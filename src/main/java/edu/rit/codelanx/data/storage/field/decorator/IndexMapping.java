package edu.rit.codelanx.data.storage.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.storage.field.FieldIndicies;

public class IndexMapping<S extends State, E> extends FieldIndex<E> {

    public IndexMapping(DataField<E> parent) {
        super(parent);
    }

    @Override
    public FieldIndicies getIndexType() {
        return null;
    }
}
