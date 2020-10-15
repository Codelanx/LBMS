package edu.rit.codelanx.data;

import edu.rit.codelanx.data.loader.ProxiedStateBuilder;
import edu.rit.codelanx.data.loader.StorageAdapter;
import edu.rit.codelanx.data.loader.Query;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.cache.RelativeStorage;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Represents a loadable source of data / {@link State} which can be interacted
 * with and saved back to the original source
 *
 * @author sja9291  Spencer Alderman
 */
public interface DataSource {

    /**
     * Inserts a new state into the system, built from dynamic input or otherwise.
     * This method will allow the underlying storage to determine a new ID for the
     * state
     *
     * @param builder A {@link StateBuilder} of the relevant state to insert
     * @param <R> The type of the {@link State} that results from being inserted
     * @return The newly inserted {@link State}
     */
    public <R extends State> R insert(StateBuilder<R> builder);

    /**
     * Given current design considerations, we are managing this around a single
     * "Library" instance per system. Thus, the Library represents more or less
     * system-wide values, such as the money for the library's account
     *
     * @return The relevant {@link Library} for the LBMS system
     */
    public Library getLibrary();

    /**
     * Loads and initializes any required data
     *
     * @throws IOException if initialization failed
     */
    public void initialize() throws IOException;

    /**
     * Cleans up any data used by the data source, saving if necessary
     *
     * @throws IOException If the data cannot be saved
     */
    public void cleanup() throws IOException;

    /**
     * Initiates a {@link Query Query<R>} of this source of data, returning all
     * {@link State} objects which match the specified parameters of the query
     *
     * @param type A {@link Class} type token for {@code <R>}
     * @param <R> The type of {@link State} to query for
     * @return A {@link Query Query<R>} that can be executed to obtain results
     */
    public <R extends State> Query<R> query(Class<R> type);

    /**
     * Get the {@link StorageAdapter} for this source of data, which adapts
     * and backs specific types of data sources for use under this
     * general-purpose wrapper / façade
     *
     * @return The {@link StorageAdapter} responsible for interacting with
     *         the data's source
     */
    public StorageAdapter getAdapter();

    /**
     * Inserts a state created from a different {@link DataSource} into the
     * {@link StorageAdapter} and {@link RelativeStorage} that back this
     * {@link DataSource}. The newly generated state will have a new ID,
     * relative to the {@link DataSource} it was inserted into
     *
     * @param state A {@link State} from an external {@link DataSource}
     * @param <R> The type of the {@code state}
     * @return The newly created {@link State} that resides on this
     *         {@link DataSource}
     * @see #insert(StateBuilder)
     */
    default public <R extends State> R insert(R state) {
        return this.insert(new ProxiedStateBuilder<>(state));
    }

    /**
     * Returns all of the stored data relevant to the given {@link Class type}.
     *
     * @param type The type of {@link State} to retrieve
     * @param <R> The generic type witness to {@code type}
     * @return A {@link Stream Stream<R>} of all possible, loaded values.
     */
    default public <R extends State> Stream<? extends R> ofLoaded(Class<R> type) {
        return this.query(type).local().results();
    }

}
