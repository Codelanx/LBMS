package edu.rit.codelanx.data;

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
