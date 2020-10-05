package edu.rit.codelanx.data.storage.field;

import com.codelanx.commons.data.SQLBiFunction;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.loader.InputMapper;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.StateType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface DataField<T> {

    default public String getName() {
        return this.getInitializer().getName();
    }

    default public Class<T> getType() {
        return this.getInitializer().getType();
    }

    public Object serialize(State state);

    public void initialize(State state, Object value);

    public void forget(State state);

    public T mutate(State state, UnaryOperator<T> updater);

    public T get(State state);

    public T set(State state, T value);

    public FieldInitializer<T> getInitializer();

    public Stream<? extends State> findStatesByValue(T key);

    default public T getFromSQL(ResultSet sql, DataStorage from) throws SQLException {
        Class<T> type = this.getType();
        return InputMapper.toTypeOrState(from, type, InputMapper.getObject(type, sql, this.getName()));
    }

    @Deprecated
    default public Stream<T> getAll(State state) {
        return Stream.of(this.get(state));
    }

    default public boolean hasModifier(FieldModifier modifier) {
        return false;
    }

    default public boolean isImmutable() {
        return false;
    }

    default public boolean isKey() {
        return false;
    }

    default public boolean isUnique() {
        return false;
    }

    @Deprecated
    default public <R> DataField<R> asField(Class<R> type) {
        Class<T> current = this.getInitializer().getType();
        if (current != type) {
            throw new ClassCastException("Cannot cast " + current.getName() + " to " + type.getName());
        }
        return (DataField<R>) this;
    }

    public static <R> Builder<R> builder(Class<R> type) {
        return new Builder<>(type);
    }

    public static <R> DataField<R> buildSimple(Class<R> type, String name, FieldModifier... modifiers) {
        return DataField.builder(type)
                .name(name)
                .modifiers(modifiers)
                .build();
    }

    public static <R extends State, T> DataField<R> buildFromState(Class<R> type, String name, DataField<T> other, FieldModifier... modifiers) {
        return DataField.builder(type)
                .name(name)
                .modifiers(modifiers)
                .mapTo(type, other)
                .build();
    }

    public static DataField<Long> makeIDField(State.Type type) {
        return DataField.makeIDField(type::getNextID);
    }

    public static DataField<Long> makeIDField(Supplier<? extends Long> nextID) {
        return DataField.builder(Long.class)
                .name("id")
                .modifiers(FieldModifier.FM_IMMUTABLE, FieldModifier.FM_UNIQUE, FieldModifier.FM_KEY)
                .giveDefaultValue(nextID)
                .build();
    }

    public static DataField<Long> makeIDField(Class<? extends State> state) {
        State.Type type = StateType.fromClass(state);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + state);
        }
        return makeIDField(type);
    }

    public static class Builder<R> {

        private static final FieldModifier[] EMPTY = new FieldModifier[0];

        protected final Class<R> type;
        private String name;
        private Supplier<? extends R> defaultValue = null;
        private FieldModifier[] modifiers = EMPTY;

        public Builder(Class<R> type) {
            if (type == null) {
                throw new NullPointerException("Cannot build from a null type");
            }
            this.type = type;
        }

        public Builder<R> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<R> modifiers(FieldModifier... modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder<R> giveDefaultValue(Supplier<? extends R> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @SuppressWarnings("unchecked") //It is checked, right below
        public <E, S extends State> MappingBuilder<S, E> mapTo(Class<S> state, DataField<E> other) {
            if (this.type != state) {
                throw new IllegalArgumentException("Class<S> input to #mapTo must match the builder's type");
            }
            return new MappingBuilder<>((Builder<S>) this, other);
        }

        public boolean isValid() {
            return this.name != null && this.modifiers != null;
        }

        public DataField<R> build() {
            FieldInitializer<R> init = new FieldInitializer<>(this.name, this.type, this.defaultValue);
            DataField<R> back = new DataFieldLoader<>(init);
            for (FieldModifier fm : modifiers) {
                back = fm.map(back);
            }
            return back;
        }
    }

    public static class MappingBuilder<T extends State, E> extends Builder<T> {

        private final Builder<T> from;
        private final DataField<E> to;

        public MappingBuilder(Builder<T> from, DataField<E> to) {
            super(from.type);
            this.from = from;
            this.to = to;
        }

        @Override
        public MappingBuilder<T, E> name(String name) {
            this.from.name(name);
            return this;
        }

        @Override
        public MappingBuilder<T, E> giveDefaultValue(Supplier<? extends T> defaultValue) {
            this.from.giveDefaultValue(defaultValue);
            return this;
        }

        @Override
        public MappingBuilder<T, E> modifiers(FieldModifier... modifiers) {
            this.from.modifiers(modifiers);
            return this;
        }

        @Override
        public boolean isValid() {
            return this.from.isValid();
        }

        @Override
        public DataField<T> build() {
            FieldInitializer<T> init = new FieldInitializer<>(this.from.name, this.from.type, this.from.defaultValue);
            DataField<T> back = new DataFieldLoader<>(this.to, init);
            for (FieldModifier fm : this.from.modifiers) {
                back = fm.map(back);
            }
            return back;
        }
    }
}
