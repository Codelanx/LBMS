package edu.rit.codelanx.data.state;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class UpdatableState implements State {

    private final AtomicBoolean modified = new AtomicBoolean(false);
    protected final long id;

    public UpdatableState(long id) {
        this.id = id;
    }

    protected final void flagModified() {
        this.modified.set(true);
    }

    public boolean isModified() {
        return this.modified.get();
    }

    @Override
    public long getID() {
        return this.id;
    }
}
