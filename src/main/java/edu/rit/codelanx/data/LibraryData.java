package edu.rit.codelanx.data;

import edu.rit.codelanx.ConfigKey;
import edu.rit.codelanx.data.loader.FFStorageAdapter;
import edu.rit.codelanx.data.loader.SQLStorageAdapter;
import edu.rit.codelanx.data.loader.StorageAdapter;
import edu.rit.codelanx.data.loader.Query;
import edu.rit.codelanx.data.loader.StateQuery;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.storage.RelativeStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class LibraryData implements DataStorage {

    private final Map<Class<? extends State>, Map<Long, State>> data = new HashMap<>();
    private final StorageAdapter adapter;
    private final RelativeStorage relative;

    public LibraryData() {
        this(Optional.ofNullable(ConfigKey.STORAGE_TYPE.as(String.class))
                    .filter(s -> !"sql".equalsIgnoreCase(s))
                    //hackaround with a type witness
                    .<Function<DataStorage, StorageAdapter>>map(s -> data -> new FFStorageAdapter(data, s))
                    .orElse(SQLStorageAdapter::new));
    }

    public LibraryData(Function<DataStorage, StorageAdapter> adapter) {
        this.adapter = adapter.apply(this);
        this.relative = new RelativeStorage(this);
    }

    @Override
    public void initialize() throws IOException {
        this.adapter.loadAll();
    }

    @Override
    public StorageAdapter getAdapter() {
        return this.adapter;
    }

    @Override
    public RelativeStorage getRelativeStorage() {
        return this.relative;
    }

    @Override
    public <R extends State> Query<R> query(Class<R> type) {
        return new StateQuery<>(this, type);
    }

    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        R back = this.adapter.insert(builder);
        this.modStates(back.getClass()).put(back.getID(), back);
        return back;
    }

    //warns you if you try to modify the elements
    @SuppressWarnings("unchecked") //we control this map, and the (bounded) elements within it
    private <R extends State> Map<Long, R> getStates(Class<R> type) {
        return (Map<Long, R>) this.data.computeIfAbsent(type, k -> new HashMap<>());
    }

    //whilst very similar to the above, this method will warn you if you try to retrieve from it
    @SuppressWarnings("unchecked")
    private Map<Long, ? super State> modStates(Class<? extends State> type) {
        return (Map<Long, ? super State>) this.getStates(type);
    }

    @Override
    public <R extends State> Stream<? extends R> ofLoaded(Class<R> type) {
        return this.getStates(type).values().stream();
    }

    @Override
    public Library getLibrary() {
        return this.adapter.getLibrary();
    }

}
