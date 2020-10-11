package edu.rit.codelanx.data.state;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * provides interface for data processing of a state
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
     * @return {@link DataStorage}
     */
    public DataStorage getLoader();

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
        public String getName();
        public <T extends State> Class<T> getConcreteType(); //bit dangerous doing it like this
        public State mapFromSQL(DataStorage storage, ResultSet set) throws SQLException;
        public State mapFromFile(DataStorage storage, Map<String, Object> file);
        public long getNextID();

    }
}
