package edu.rit.codelanx.data.state;

import edu.rit.codelanx.data.DataStorage;

public abstract class StateBuilder<T extends State> {

    private final DataStorage storage;

    public StateBuilder(DataStorage storage) {
        this.storage = storage;
    }

    public final T build() {
        return this.storage.insert(this);
    }

    public abstract boolean isValid();

    public abstract Object[] asSQLArguments();

    protected abstract T buildObj(long id);
}
