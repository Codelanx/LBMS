package edu.rit.codelanx.data.state;

import com.codelanx.commons.data.SQLBiFunction;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.loader.InputMapper;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.data.state.types.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

/**
 * provides interface for data processing of a state
 */
public interface State {

    public DataField<Long> getIDField();
    public DataField<? super Object>[] getFields();
    public Type getType();
    public String toFormattedText();
    public DataStorage getLoader();
    public boolean isValid();
    public void unload(); //unloads this value, invalidating it
    //returns the ID for the relevant state, helps with indexing
    default public long getID() {
        return this.getIDField().get(this);
    }

    public interface Type {

        public String getName();
        public <T extends State> Class<T> getConcreteType(); //bit dangerous doing it like this
        public State mapFromSQL(DataStorage storage, ResultSet set) throws SQLException;
        public State mapFromFile(DataStorage storage, Map<String, Object> file);
        public long getNextID();

    }
}
