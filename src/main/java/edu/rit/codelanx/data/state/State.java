package edu.rit.codelanx.data.state;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.SQLBiFunction;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.loader.StateBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A marker for the various operations and data held by an object within our
 * system
 *
 * @author sja9291  Spencer Alderman
 */
public interface State extends FileSerializable, Comparable<State> {
    /**
     * gets the field id
     * @return id of {@link DataField}
     */
    public DataField<Long> getIDField();

    /**
     * gets the values of the field
     * @return field values
     */
    public DataField<? super Object>[] getFields();

    /**
     * gets the field type.
     * @return current {@link Type}
     */
    public Type getType();

    /**
     * String representation of the current state
     * @return string of state data
     */
    public String toFormattedText();

    /**
     * gets the data storage
     * @return {@link DataSource}
     */
    public DataSource getLoader();

    /**
     * checks for the validity of the current state
     * @return true of valid, otherwise false.
     */
    public boolean isValid();

    /**
     * unloads this value, invalidating it.
     */
    public void unload();

    /**
     * returns id for the relevant state
     * @return id of type long
     */
    default public long getID() {
        return this.getIDField().get(this);
    }

    @Override
    default int compareTo(State o) {
        if (!this.getType().getConcreteType().isInstance(o)) {
            throw new IllegalArgumentException("Cannot compare different State types");
        }
        return Long.compare(this.getID(), o.getID());
    }

    /**
     * Interface for types
     */
    public interface Type {
        /**
         * gets the name of the state
         * @return string name in lower case
         */
        public String getName();
        /**
         * gets the state concrete type
         * @param <T> extends {@link State}
         * @return the type
         */
        public <T extends State> Class<T> getConcreteType(); //bit dangerous doing it like this

        /**
         * Gets the constructor for a new {@link State} via a
         * {@link StateBuilder} instance
         *
         * @return A constructor for the {@link State} relevant to this type
         */
        public <T extends State> StateBuildConstructor<T> getBuilderConstructor();


        /**
         * Gets the constructor for a new {@link State} via a building from
         * an sql {@link ResultSet}
         *
         * @return A constructor for the {@link State} relevant to this type
         */
        public <T extends State> StateSQLConstructor<T> getSQLConstructor();

        /**
         * Gets the constructor for a new {@link State} via a building from
         * a file's {@link Map Map<String, Object>} representation
         *
         * @return A constructor for the {@link State} relevant to this type
         */
        public <T extends State> StateFileConstructor<T> getFileConstructor();

        /**
         * increments to get the next ID
         * @return next ID of type long
         */
        //TODO: fix for unique ids per server? eh not really important
        public long getNextID();

    }

    /**
     * An interface for creating a new {@link State} from a builder
     *
     * @param <T> The type of {@link State} to create
     */
    @FunctionalInterface
    public interface StateBuildConstructor<T extends State> {

        /**
         * Creates a new {@link State}
         *
         * @param storage The {@link DataSource} to store the state on
         * @param id The {@code long} identifier for the state
         * @param builder A valid {@link StateBuilder}
         * @return The newly created {@link State}
         * @see State#getID()
         * @see StateBuilder
         */
        public T create(DataSource storage, long id, StateBuilder<T> builder);
    }

    /**
     * An interface for loading a {@link State} from SQL storage
     *
     * @param <T> The type of {@link State} to create
     */
    @FunctionalInterface
    public interface StateSQLConstructor<T extends State> extends SQLBiFunction<DataSource, ResultSet, T> {

        /**
         * Creates a new {@link State}
         *
         * @param storage The {@link DataSource} to store the state on
         * @param sql The {@link ResultSet} to grab {@link DataField} info from
         * @return The newly created {@link State}
         * @see DataField
         */
        public T create(DataSource storage, ResultSet sql) throws SQLException;


        /**
         * A façade for {@link SQLBiFunction} applications
         *
         * {@inheritDoc}
         * @param storage {@inheritDoc}
         * @param rs {@inheritDoc}
         * @return {@inheritDoc}
         * @see #create(DataSource, ResultSet) for the actual method
         */
        @Override
        default T apply(DataSource storage, ResultSet rs) throws SQLException {
            return this.create(storage, rs);
        }
    }

    /**
     * An interface for loading a {@link State} from flatfile storage
     *
     * @param <T> The type of {@link State} to create
     */
    @FunctionalInterface
    public interface StateFileConstructor<T extends State> extends BiFunction<DataSource, Map<String, Object>, T> {

        /**
         * Creates a new {@link State}
         *
         * @param storage The {@link DataSource} to store the state on
         * @param file The {@link Map Map<String, Object>} representation of the
         *             State, from {@link State#serialize()}
         * @return The newly created {@link State}
         * @see DataField
         * @see FileSerializable#serialize()
         */
        public T create(DataSource storage, Map<String, Object> file);

        /**
         * A façade for {@link BiFunction} applications
         *
         * {@inheritDoc}
         * @param storage {@inheritDoc}
         * @param file {@inheritDoc}
         * @return {@inheritDoc}
         * @see #create(DataSource, Map) for the actual method
         */
        @Override
        default T apply(DataSource storage, Map<String, Object> file) {
            return this.create(storage, file);
        }
    }
}
