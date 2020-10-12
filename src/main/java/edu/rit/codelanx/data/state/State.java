package edu.rit.codelanx.data.state;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * A marker for the various operations and data held by an object within our
 * system
 *
 * @author sja9291  Spencer Alderman
 */
public interface State {
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
         * calls (DataStorage storage, ResultSet set) constructor within the state
         * @param storage-{@link DataSource}
         * @param set- {@link ResultSet}
         * @return of type {@link State}
         * @throws SQLException when errors occur
         */
        public State mapFromSQL(DataSource storage, ResultSet set) throws SQLException;
        /**
         * calls (DataStorage storage, Map<String, Object> file) constructor within the state
         * @param storage-{@link DataSource}
         * @param file- in the form of a map
         * @return of type {@link State}
         * @throws SQLException when errors occur
         */
        public State mapFromFile(DataSource storage, Map<String, Object> file);

        /**
         * increments to get the next ID
         * @return next ID of type long
         */
        public long getNextID();

    }
}
