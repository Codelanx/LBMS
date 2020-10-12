package edu.rit.codelanx.data.loader;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.state.types.StateType;
import edu.rit.codelanx.data.storage.StateStorage;
import edu.rit.codelanx.data.storage.field.DataField;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * An adapter for a {@link DataStorage} backed by some specific file or service
 *
 * @author sja9291  Spencer Alderman
 */
public interface StorageAdapter {

    /**
     * Returns the singular {@link Library} associated with this system,
     * creating it if it does not exist yet
     *
     * @return The sole {@link Library} instance for this system
     */
    public Library getLibrary(); //this is essentially a primary class

    /**
     * Inserts a new {@link State} into this {@link StorageAdapter} with the
     * supplied {@link StateBuilder StateBuilder<R>}
     *
     * @param builder The {@link StateBuilder} to build the new state from
     * @param <R> The type of {@link State} that results from this insertion
     * @return The newly created {@link State}
     */
    public <R extends State> R insert(StateBuilder<R> builder);

    /**
     * Adds a state loaded from an external {@link DataStorage}, and adds it to
     * the {@link DataStorage} backed by this adapter
     *
     * @param external The {@link State} to insert into our source data
     * @param <R> The type of {@link State} to insert
     * @return The newly created state in the system
     * @throws IllegalArgumentException if the state was loaded by this adapter
     */
    default public <R extends State> R insert(R external) {
        if (external.getLoader() == this.getAdaptee()) {
            throw new IllegalArgumentException("State was loaded from this adapter");
        }
        State back = this.insert(StateType.makeBuilder(external.getClass(), external.getIDField(), external.getFields()));
        return (R) back;
    }

    /**
     * Loads all data from the source backed by this adapter, if applicable
     *
     * @throws IOException If the data could not be loaded
     */
    public void loadAll() throws IOException;

    /**
     * Saves all data to the appropriate locations, if necessary
     *
     * @throws IOException If the data could not be saved
     */
    public void saveAll() throws IOException;

    /**
     * Returns the {@link DataStorage} that this adapter is applied to
     *
     * REFACTOR: Better descriptions
     *
     * @return The {@link DataStorage} that we supply data for
     */
    public DataStorage getAdaptee();

    /**
     * Loads the given states from storage if necessary, returning all valid
     * states for the given query. The default implementation will only search
     * through local storage, but this method may block if necessary
     *
     * @param query The {@link Query Query<R>} to execute for this adapter
     * @param <R> The type of {@link State} to query for
     * @return A {@link Stream Stream<R>} of results matching the specified
     *         {@code query}
     */
    default public <R extends State> Stream<R> handleQuery(StateQuery<R> query) {
        Class<R> type = query.getType();
        StateStorage<R> data = this.getAdaptee().getRelativeStorage().getStateStorage(type);
        return query.locateLocal(data);
    }

    /**
     * Loads a state from a remote data source. This method will specifically
     * skip any caches within the system and go straight to the source.
     * Additionally, if this adapter is a cached adapter, then nothing is done,
     * and {@code null} is returned
     *
     * @param id The id of the {@link State} to load
     * @param type A type token for this method to access {@code <R>}
     * @param <R> The type of {@link State} to load
     * @return The state as it exists in the remote source, or {@code null} if
     *         {@link #isCached()} == {@code true}, or if there is no state
     *         by the given {@code id}
     */
    public <R extends State> R loadState(long id, Class<R> type);

    /**
     * Loads a state from a remote data source. This method will specifically
     * skip any caches within the system and go straight to the source.
     * Additionally, if this adapter is a cached adapter, then nothing is done,
     * and {@link Stream#empty()} is returned
     *
     * @param type A type token for this method to access {@code <R>}
     * @param field The {@link DataField} to search through
     * @param value The value of the {@code field} to filter by
     * @param <R> The type of {@link State} we are loading
     * @param <E> The type of data held by {@code field} and {@code value}
     * @return A {@link Stream Stream<R>} of freshly loaded states, as they
     *         exist in the data source, or {@link Stream#empty()} if
     *         {@link #isCached()} == {@code true}, or if no states exist with
     *         the provided {@code value}
     * @see #loadState(long, Class)
     */
    public <R extends State, E> Stream<R> loadState(Class<R> type, DataField<E> field, E value);

    /**
     * Notifies the adapter of a change in a given {@link State}'s
     * {@link DataField fields}, allowing the adapter to update a remote
     * source if necessary
     *
     * @param state The {@link State} that was modified
     * @param field The {@link DataField} that was updated
     * @param value The new value that was set
     * @param <E> The type of the updated {@code value}
     */
    // May also happen if someone calls Field#mutate, and the Field points to a
    // State which is simply mutated (not re-referenced)
    public <E> void notifyUpdate(State state, DataField<E> field, E value);

    /**
     * Removes a provided {@link State} from the backed data source, effectively
     * deleting it
     *
     * @param state The {@link State} to remove
     */
    public void remove(State state);

    /**
     * Returns whether this adapter preloads all data into memory throughout the
     * runtime of the program (e.g. flatfiles)
     *
     * @return {@code true} if loaded at all times, {@code false} otherwise
     */
    public boolean isCached();
}
