package edu.rit.codelanx.data.cache.field;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * provides initialization of a data field
 *
 * @param <T> type of the data
 * @author sja9291  Spencer Alderman
 */
public class FieldInitializer<T> {

    private final String name;
    private final Class<T> type;
    private final Supplier<? extends T> defaultValue;

    /**
     * initializes field
     *
     * @param name         string of the field
     * @param type         {@link T} of the field
     * @param defaultValue of the field
     */
    public FieldInitializer(String name,
                            Class<T> type,
                            Supplier<? extends T> defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * gets the field' name
     *
     * @return string name
     */
    public String getName() {
        return this.name;
    }

    /**
     * gets the field's type
     *
     * @return type {@link Class<T>}
     */
    public Class<T> getType() {
        return this.type;
    }

    /**
     * gets the default value of the object
     *
     * @return default {@link T} value
     * @throws UnsupportedOperationException If object has no default value
     */
    public T getDefaultValue() {
        if (this.defaultValue == null) {
            throw new UnsupportedOperationException("Object has no default value");
        }
        return this.defaultValue.get();
    }

}
