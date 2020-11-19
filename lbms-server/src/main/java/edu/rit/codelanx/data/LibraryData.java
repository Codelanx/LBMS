package edu.rit.codelanx.data;

import edu.rit.codelanx.ConfigKey;
import edu.rit.codelanx.data.storage.FFStorageAdapter;
import edu.rit.codelanx.data.storage.SQLStorageAdapter;
import edu.rit.codelanx.data.storage.StorageAdapter;
import edu.rit.codelanx.data.storage.Query;
import edu.rit.codelanx.data.storage.StateQuery;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.storage.StateBuilder;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.cache.RelativeStorage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A concrete {@link DataSource} for loading {@link State} objects specific
 * to our LBMS implementation, such as Author, Book, Visitor, etc.
 *
 * @author sja9291  Spencer Alderman
 */
public class LibraryData implements DataSource {

    //the background loader
    private final StorageAdapter adapter;
    //our indexed storage
    private final RelativeStorage relative;

    /**
     * Selects an appropriate {@link StorageAdapter} for general LBMS data,
     * based on the user settings reflected in {@link ConfigKey}
     *
     * @see ConfigKey
     */
    public LibraryData() {
        this(Optional.ofNullable(ConfigKey.STORAGE_TYPE.as(String.class))
                    .filter(s -> !"sql".equalsIgnoreCase(s))
                    //hackaround with a type witness
                    .<Function<DataSource, StorageAdapter>>map(s -> data -> new FFStorageAdapter(data, s))
                    .orElse(SQLStorageAdapter::new));
    }

    /**
     * Constructs a source of LBMS-related data using a {@link StorageAdapter}
     * that is provided to this concrete {@link DataSource}
     *
     * @param adapter The {@link StorageAdapter} to load from
     */
    public LibraryData(Function<DataSource, StorageAdapter> adapter) {
        this.adapter = adapter.apply(this);
        this.relative = new RelativeStorage(this);
    }

    /**
     * {@inheritDoc}
     * @throws IOException {@inheritDoc}
     * @see StorageAdapter#loadAll()
     */
    @Override
    public void initialize() throws IOException {
        this.adapter.loadAll();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public StorageAdapter getAdapter() {
        return this.adapter;
    }

    /**
     * {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public void cleanup() throws IOException {
        this.adapter.saveAll();
        //TODO: Cleanup RelativeStorage as well since it's hard references
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public RelativeStorage getRelativeStorage() {
        return this.relative;
    }

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param <R> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State> Query<R> query(Class<R> type) {
        return new StateQuery<>(this, type);
    }

    /**
     * {@inheritDoc}
     * @param builder {@inheritDoc}
     * @param <R> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        R back = this.adapter.insert(builder);
        this.relative.addState(back);
        return back;
    }

    /**
     * {@inheritDoc}
     * @param state {@inheritDoc}
     * @param <R> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State> R insert(R state) {
        R back = this.adapter.insert(state);
        this.relative.addState(back);
        return back;
    }

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param <R> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State> Stream<? extends R> ofLoaded(Class<R> type) {
        return this.relative.getStateStorage(type).streamLoaded();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Library getLibrary() {
        return this.adapter.getLibrary();
    }

}
