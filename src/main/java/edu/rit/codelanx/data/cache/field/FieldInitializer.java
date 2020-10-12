package edu.rit.codelanx.data.cache.field;

import java.util.function.Supplier;

public class FieldInitializer<T> {

    private final String name;
    private final Class<T> type;
    private final Supplier<? extends T> defaultValue;

    public FieldInitializer(String name,
                            Class<T> type,
                            Supplier<? extends T> defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getType() {
        return this.type;
    }

    public T getDefaultValue() {
        if (this.defaultValue == null) {
            throw new UnsupportedOperationException("Object has no default value");
        }
        return this.defaultValue.get();
    }

}
