package edu.rit.codelanx.data;

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
