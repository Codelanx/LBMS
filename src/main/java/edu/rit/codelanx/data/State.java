package edu.rit.codelanx.data;

public interface State {

    public long getID(); //returns the ID for the relevant state, helps with indexing
    public Object[] toFields();
}
