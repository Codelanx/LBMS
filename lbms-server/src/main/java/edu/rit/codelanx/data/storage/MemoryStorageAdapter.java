package edu.rit.codelanx.data.storage;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.state.types.Library;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

//Stores data in-memory, starts empty
public class MemoryStorageAdapter implements StorageAdapter {

    private final Map<Class<? extends State>, Map<Long, ? extends State>> data = new HashMap<>();
    private final DataSource storage;
    private final Library lib;

    public MemoryStorageAdapter(DataSource storage) {
        this.storage = storage;
        this.lib = Library.create()
                .setValue(Library.Field.MONEY, BigDecimal.ZERO)
                .build(storage);
    }

    protected <R extends State> Map<Long, R> getData(Class<R> type) {
        return (Map<Long, R>) data.computeIfAbsent(type, k -> new HashMap<>());
    }

    @Override
    public Library getLibrary() {
        return this.lib;
    }

    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        R back = builder.buildObj(this.storage, builder.getType().getNextID());
        this.getData(builder.getType().getConcreteType()).put(back.getID(), back);
        return back;
    }

    @Override
    public void loadAll() throws IOException {
        //no-op
    }

    @Override
    public void saveAll() throws IOException {
        //no-op
    }

    @Override
    public DataSource getAdaptee() {
        return this.storage;
    }

    @Override
    public <R extends State> R loadState(long id, Class<R> type) {
        return this.getData(type).get(id);
    }

    @Override
    public <R extends State, E> Stream<R> loadState(Class<R> type, DataField<E> field, E value) {
        return (Stream<R>) field.findStatesByValue(value); //TODO: Make sure this doesn't loop
    }

    @Override
    public <E> void notifyUpdate(State state, DataField<E> field, E value) {
        //no-op
    }

    @Override
    public void remove(State state) {
        this.getData(state.getClass()).remove(state.getID());
    }

    @Override
    public boolean isCached() {
        return true;
    }
}
