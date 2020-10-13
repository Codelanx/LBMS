package edu.rit.codelanx.data.cache.field;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.loader.InputMapper;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.StateType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Interfaces contains functionalities handling specified {@link T} field
 *
 * @param <T> field type
 * @author sja9291  Spencer Alderman
 */
public interface DataField<T> {

    /**
     * gets the name of the data field
     *
     * @return string name
     */
    default public String getName() {
        return this.getInitializer().getName();
    }

    /**
     * gets data field's type
     *
     * @return a specific type
     */
    default public Class<T> getType() {
        return this.getInitializer().getType();
    }

    /**
     * serializes the specified state
     *
     * @param state the specified {@link State}
     * @return serialized object
     */
    public Object serialize(State state);

    /**
     * initializes Data field with A specific state and its value.
     *
     * @param state involved {@link State}
     * @param value {@link Object} of the above State
     */
    public void initialize(State state, Object value);

    /**
     * clear out the state.
     *
     * @param state {@link State} to be cleared out.
     */
    public void forget(State state);

    /**
     * makes changes to the data field
     *
     * @param state   {@link State} that to be changed
     * @param updater {@link UnaryOperator} that forms the operation
     * @return changed {@link T}
     */
    public T mutate(State state, UnaryOperator<T> updater);


    /**
     * retrieves the data in a specified state
     *
     * @param state {@link State} to retrieve data from
     * @return data {@link T}
     */
    public T get(State state);

    /**
     * sets the field of this state to a certain value
     *
     * @param state {@link State} to set the data from
     * @param value to be {@link T}
     * @return
     */
    public T set(State state, T value);

    /**
     * gets he initialized version of the data field
     *
     * @return {@link FieldInitializer} of type {@link T}
     */
    public FieldInitializer<T> getInitializer();

    /**
     * finds the state based on specified value
     *
     * @param key {@link T} used to search up the state
     * @return {@link Stream} of type {@link State}
     */
    public Stream<? extends State> findStatesByValue(T key);

    default public T getFromSQL(ResultSet sql, DataSource from) throws SQLException {
        Class<T> type = this.getType();
        return InputMapper.toTypeOrState(from, type, InputMapper.getObject(type, sql, this.getName()));
    }


    @Deprecated
    default public Stream<T> getAll(State state) {
        return Stream.of(this.get(state));
    }


    /**
     * checks for data field index
     *
     * @param modifier {@link FieldIndicies}
     * @return {@code true} if has index, {@code false} otherwise
     */
    default public boolean hasIndex(FieldIndicies modifier) {
        return false;
    }

    /**
     * checks for mutability of the data field
     *
     * @return {@code false} since this's mutable
     */
    default public boolean isImmutable() {
        return false;
    }

    /**
     * checks if the data field can be found via cache
     *
     * @return {@code false}  since it can't
     */
    default public boolean isKey() {
        return false;
    }

    /**
     * checks the for the Data field uniqueness
     *
     * @return {@code false} since data field is not unique
     */
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

    public static <R> DataField<R> buildSimple(Class<R> type, String name, FieldIndicies... modifiers) {
        return DataField.builder(type)
                .name(name)
                .modifiers(modifiers)
                .build();
    }

    public static <R extends State, T> DataField<R> buildFromState(Class<R> type, String name, DataField<T> other, FieldIndicies... modifiers) {
        return DataField.builder(type)
                .name(name)
                .modifiers(modifiers)
                .mapTo(type, other)
                .build();
    }

    /**
     * creates id type long
     * @param type of {@link State} involved
     * @return {@link DataField} long type
     */
    public static DataField<Long> makeIDField(State.Type type) {
        return DataField.makeIDField(type::getNextID);
    }

    /**
     * called in {@link DataField#makeIDField(State.Type)} to creates {@link DataField} type long
     * @param nextID-subsequent id of specified {@link State.Type}
     * @return {@link DataField} type long
     */
    public static DataField<Long> makeIDField(Supplier<? extends Long> nextID) {
        return DataField.builder(Long.class)
                .name("id")
                .modifiers(FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_UNIQUE, FieldIndicies.FM_KEY)
                .giveDefaultValue(nextID)
                .build();
    }


    public static DataField<Long> makeIDField(Class<? extends State> state) {
        State.Type type = StateType.fromClass(state);
        return makeIDField(type);
    }

    public static class Builder<R> {

        private static final FieldIndicies[] EMPTY = new FieldIndicies[0];

        protected final Class<R> type;
        private String name;
        private Supplier<? extends R> defaultValue = null;
        private FieldIndicies[] modifiers = EMPTY;

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

        public Builder<R> modifiers(FieldIndicies... modifiers) {
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
            for (FieldIndicies fm : modifiers) {
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
        public MappingBuilder<T, E> modifiers(FieldIndicies... modifiers) {
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
            for (FieldIndicies fm : this.from.modifiers) {
                back = fm.map(back);
            }
            return back;
        }
    }
}
