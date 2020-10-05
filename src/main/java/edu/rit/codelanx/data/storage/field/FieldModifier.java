package edu.rit.codelanx.data.storage.field;

import edu.rit.codelanx.data.storage.field.decorator.DecoratorImmutable;
import edu.rit.codelanx.data.storage.field.decorator.DecoratorKey;
import edu.rit.codelanx.data.storage.field.decorator.DecoratorUnique;
import edu.rit.codelanx.data.storage.field.decorator.FieldDecorator;

import java.util.function.Function;

public enum FieldModifier {
    FM_IMMUTABLE(DecoratorImmutable::new), //should not be modified
    FM_KEY(DecoratorKey::new), //key that is used for object lookups
    FM_UNIQUE(DecoratorUnique::new), //only one value per state
    ;

    private final Function<DataField<?>, FieldDecorator<?>> mapper;

    @SuppressWarnings("unchecked") //we use the methods for type safety checks
    private <T> FieldModifier(Function<DataField<T>, FieldDecorator<T>> mapper) {
        this.mapper = //java isn't cool enough to handle this
                (Function<DataField<?>, FieldDecorator<?>>)
                (Function<?, ?>) mapper;
    }

    //constructor validates DataField<T> -> Decorator<T>
    @SuppressWarnings("unchecked")
    public <T> DataField<T> map(DataField<T> given) {
        return (DataField<T>) this.mapper.apply(given);
    }

}
