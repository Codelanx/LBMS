package edu.rit.codelanx.data.cache.field;

import edu.rit.codelanx.data.cache.field.decorator.IndexCompositeKey;
import edu.rit.codelanx.data.cache.field.decorator.IndexImmutable;
import edu.rit.codelanx.data.cache.field.decorator.IndexKey;
import edu.rit.codelanx.data.cache.field.decorator.IndexUnique;
import edu.rit.codelanx.data.cache.field.decorator.FieldIndex;

import java.util.function.Function;

public enum FieldIndicies {
    FM_IMMUTABLE(IndexImmutable::new), //should not be modified
    FM_KEY(IndexKey::new), //key that is used for object lookups
    FM_UNIQUE(IndexUnique::new), //only one value per state
    FM_COMPOSITE(IndexCompositeKey::new), //cannot add if every ALL_UNIQUE field matches
    ;

    private final Function<DataField<?>, FieldIndex<?>> mapper;

    @SuppressWarnings("unchecked") //we use the methods for type safety checks
    private <T> FieldIndicies(Function<DataField<T>, FieldIndex<T>> mapper) {
        this.mapper = //java isn't cool enough to handle this
                (Function<DataField<?>, FieldIndex<?>>)
                (Function<?, ?>) mapper;
    }

    //constructor validates DataField<T> -> Decorator<T>
    @SuppressWarnings("unchecked")
    public <T> DataField<T> map(DataField<T> given) {
        return (DataField<T>) this.mapper.apply(given);
    }

}
