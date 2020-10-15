package edu.rit.codelanx.data.loader;

import com.codelanx.commons.data.SQLBiFunction;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.state.types.StateType;
import edu.rit.codelanx.util.Errors;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum InputMapper {;

    public static final Map<Class<?>, SQLBiFunction<ResultSet, String, ?>> HANDLES; //? == "H"

    static {
        Map<Class<?>, SQLBiFunction<ResultSet, String, ?>> handles = new HashMap<>();
        handles.put(Byte.class, ResultSet::getByte);
        handles.put(Short.class, ResultSet::getShort);
        handles.put(Integer.class, ResultSet::getInt);
        handles.put(Long.class, ResultSet::getLong);
        handles.put(String.class, ResultSet::getString);
        handles.put(Float.class, ResultSet::getFloat);
        handles.put(Double.class, ResultSet::getDouble);
        handles.put(Instant.class, ResultSet::getTimestamp);
        handles.put(Boolean.class, ResultSet::getBoolean);
        handles.put(BigDecimal.class, ResultSet::getBigDecimal);
        HANDLES = Collections.unmodifiableMap(handles);
    }

    public static Object getObject(Class<?> type, ResultSet sql, String identitier) throws SQLException {
        try {
            SQLBiFunction<ResultSet, String, ?> func = HANDLES.get(type);
            return func == null
                    ? sql.getObject(identitier)
                    : func.apply(sql, identitier);
        } catch (SQLException ex) {
            Errors.reportAndExit(ex);
            throw new RuntimeException(ex);
        }
    }

    public static Object getObject(Map<String, Object> file, String identifier) {
        return file.get(identifier);
    }

    public static Instant toInstant(Object value) {
        if (value instanceof Number) {
            return Instant.ofEpochSecond(((Number) value).longValue());
        }
        if (!(value instanceof Timestamp)) {
            throw new IllegalArgumentException("Cannot convert a non-timestamp to an Instant");
        }
        return ((Timestamp) value).toInstant();
    }

    public static <T extends State> Optional<T> toState(DataSource storage, Class<T> type, long id) {
        State.Type st = StateType.fromClass(type);
        return storage.query(type).isID(id).results().findAny();
    }

    public static <T extends State, E> T toState(DataSource storage, Class<T> type, DataField<E> field, E value) {
        if (!field.isUnique()) {
            throw new IllegalStateException("Cannot map a to a unique State based on the provided field, field is not unique");
        }
        return storage.query(type).isEqual(field, value).results().findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching field found for: " + type + " , " + field.getName()));
    }

    public static boolean isStateClass(Class<?> clazz) {
        return clazz != null && State.class.isAssignableFrom(clazz);
    }

    public static <T> T toType(Class<T> type, Object value) {
        if (type.isAssignableFrom(Instant.class)) {
            return (T) InputMapper.toInstant(value); //in this case, T == Instant
        }
        if (Instant.class.isAssignableFrom(type)) {
            return (T) InputMapper.toInstant(value);
        }
        if (type.isAssignableFrom(Integer.class) && value instanceof Number) {
            return (T) (Object) ((Number) value).intValue();
        }
        if (type.isAssignableFrom(BigDecimal.class) && value instanceof Number) {
            return (T) (Object) BigDecimal.valueOf(((Number) value).longValue());
        }
        return (T) value; //will CCE if mismatched at this point
    }

    public static <T> Optional<T> toTypeOrState(DataSource storage, Class<T> type, Object value) {
        if (InputMapper.isStateClass(type)) {
            if (!(value instanceof Number)) {
                throw new IllegalArgumentException("Cannot interpret ID for " + type.getSimpleName() + ": " + value);
            }
            //T extends State, thus the below is safe but needs casting
            //additionally, we'll move it outside since it references stuff
            return InputMapper.toState(storage, (Class<? extends State>) type, ((Number) value).longValue())
                    .map(o -> (T) o);
        }
        return Optional.of((T) InputMapper.toType(type, value));
    }
}
