package edu.rit.codelanx.data.types;

import edu.rit.codelanx.data.State;

public class Transaction implements State {
    @Override
    public long getID() {
        return 0;
    }

    @Override
    public Object[] toFields() {
        return new Object[0];
    }
}
