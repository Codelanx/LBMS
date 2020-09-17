package edu.rit.codelanx.data.types;

import edu.rit.codelanx.data.State;

public class Library implements State {

    public void open() {
        //TODO
    }

    public void close() {
        //TODO
    }

    @Override
    public long getID() {
        return 42; //OooooOooo spooky. We only have one library
    }

    @Override
    public Object[] toFields() {
        return new Object[0];
    }
}
