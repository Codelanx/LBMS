package edu.rit.codelanx.data.loader;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.storage.StateStorage;
import edu.rit.codelanx.data.storage.field.DataField;

import java.io.IOException;
import java.util.stream.Stream;

public interface StorageAdapter {

    public Library getLibrary(); //this is essentially a primary class
    public <R extends State> R insert(StateBuilder<R> builder);
    public void loadAll() throws IOException;
    public void saveAll() throws IOException;
    public DataStorage getAdaptee();
    //TODO: Update/save

    default public <R extends State> Stream<R> handleQuery(StateQuery<R> query) {
        Class<R> type = query.getType();
        StateStorage<R> data = this.getAdaptee().getRelativeStorage().getStateStorage(type);
        return query.locateLocal(data);
    }
    public <R extends State> R loadState(long id, Class<R> type);
    public <R extends State, E> Stream<R> loadState(Class<R> type, DataField<E> field, E value);
    //notified when a change occurs, along with the new value
    // May also happen if someone calls Field#mutate, and the Field points to a
    // State which is simply mutated (not re-referenced)
    public <E> void notifyUpdate(State state, DataField<E> field, E value);

    public void remove(State state);
    public boolean isCached(); //true if we keep values loaded from program start to finish (e.g. flatfiles)
}
