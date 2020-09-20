package edu.rit.codelanx.data.loader;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StateBuilder;
import edu.rit.codelanx.data.types.Library;

import java.io.IOException;
import java.util.List;

public interface StorageAdapter {

    public Library getLibrary();
    public <R extends State> List<R> getState(Class<R> type);
    public <R extends State> R insert(StateBuilder<R> builder);
    public void loadAll() throws IOException;
    //TODO: Update/save
}
