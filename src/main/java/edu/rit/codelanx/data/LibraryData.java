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
import edu.rit.codelanx.data.cache.RelativeStorage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * A concrete {@link DataSource} for loading {@link State} objects specific
 * to our LBMS implementation, such as Author, Book, Visitor, etc.
 *
 * @author sja9291  Spencer Alderman
 */
public class LibraryData implements DataSource {

    //the background loader
    private final StorageAdapter adapter;

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

    /*
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    //@Override
    //public RelativeStorage getRelativeStorage() {
    //    return this.relative;
    //}

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
        return back;
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
