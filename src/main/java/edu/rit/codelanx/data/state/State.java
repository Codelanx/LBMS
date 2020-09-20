package edu.rit.codelanx.data.state;

public interface State {

    public long getID(); //returns the ID for the relevant state, helps with indexing
    public Type getType();
    public enum Type {
        BOOK,
        CHECKOUT,
        LIBRARY,
        TRANSACTION,
        VISIT,
        VISITOR,
        UNKNOWN,
        ;
    }
}
