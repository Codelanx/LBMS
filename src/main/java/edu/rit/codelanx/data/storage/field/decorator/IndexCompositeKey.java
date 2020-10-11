package edu.rit.codelanx.data.storage.field.decorator;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.storage.field.FieldIndicies;

public class IndexCompositeKey<T> extends FieldIndex<T> {

    public IndexCompositeKey(DataField<T> parent) {
        super(parent);
    }

    @Override
    public void initialize(State state, Object value) {
        super.initialize(state, value);
    }

    @Override
    public FieldIndicies getIndexType() {
        return null;
    }
}
