package edu.rit.codelanx.data.loader;

import com.codelanx.commons.data.SQLBiFunction;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.StateType;
import edu.rit.codelanx.data.cache.StateStorage;
import edu.rit.codelanx.data.cache.StorageContainer;
import edu.rit.codelanx.data.cache.field.DataField;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StateQuery<S extends State> implements Query<S> {

    private final DataSource storage;
    private final Class<S> type;
    private final List<Comparison<?>> comparisons = new ArrayList<>();
    private final List<Predicate<S>> postFixes = new ArrayList<>();

    public StateQuery(DataSource storage, Class<S> type) {
        this.storage = storage;
        this.type = type;
    }

    @Override
    public Stream<S> results() {
        Stream<S> s = this.storage.getAdapter().handleQuery(this);
        for (Predicate<S> pred : this.postFixes) {
            s = s.filter(pred);
        }
        return s;
    }

    public Class<S> getType() {
        return this.type;
    }

    public <E> Query<S> predicate(DataField<E> field, E value, ComparisonType type) {
        this.comparisons.add(new Comparison<>(field, value, type));
        return this;
    }

    public List<Comparison<?>> getComparisons() {
        return Collections.unmodifiableList(this.comparisons);
    }

    @Override
    public <E> Query<S> filterBy(DataField<E> field, Predicate<E> test) {
        this.postFixes.add(state -> test.test(field.get(state)));
        return this;
    }

    @Override
    public <E> Query<S> isEqual(DataField<E> field, E value) {
        return this.predicate(field, value, ComparisonType.EQUALITY);
    }

    @Override
    public <E> Query<S> isAny(DataField<E> field, Iterable<E> values) {
        this.comparisons.add(new MultiComparison<>(field, values));
        return this;
    }

    @Override
    public <E extends Comparable<E>> Query<S> isLessThan(DataField<E> field, E value) {
        return this.predicate(field, value, ComparisonType.LESS_THAN);
    }

    @Override
    public <E extends Comparable<E>> Query<S> isGreaterThan(DataField<E> field, E value) {
        return this.predicate(field, value, ComparisonType.GREATER_THAN);
    }

    @Override
    public <E extends Comparable<E>> Query<S> isLessThanOrEq(DataField<E> field, E value) {
        return this.predicate(field, value, ComparisonType.LESS_THAN_OR_EQ);
    }

    @Override
    public <E extends Comparable<E>> Query<S> isGreaterThanOrEq(DataField<E> field, E value) {
        return this.predicate(field, value, ComparisonType.GREATER_THAN_OR_EQ);
    }

    public enum ComparisonType {
        EQUALITY("=", Objects::equals),
        LESS_THAN("<", (o1, o2) -> o1.compareTo(o2) < 0),
        GREATER_THAN(">", (o1, o2) -> o1.compareTo(o2) > 0),
        LESS_THAN_OR_EQ("<=", (o1, o2) -> o1.compareTo(o2) <= 0),
        GREATER_THAN_OR_EQ(">=", (o1, o2) -> o1.compareTo(o2) >= 0),
        IS_ANY("IN"),
        ;

        private final String operator;
        private final BiPredicate<Object, Object> compare;

        private <T extends Comparable<T>> ComparisonType(String operator, BiPredicate<T, T> compare) {
            this.operator = operator;
            //warning left, this isn't safe if you use it incorrectly
            //(should only get comparables unless IS_ANY or EQUALITY)
            this.compare = (BiPredicate<Object, Object>) (BiPredicate<?, ?>) compare;
        }

        private ComparisonType(String operator) {
            this(operator, null);
        }

        public String getOperator() {
            return this.operator;
        }

        public <T> boolean compare(T one, T two) {
            if (this == IS_ANY) {
                throw new UnsupportedOperationException("Cannot calculate comparison for a one-to-many relationship");
            }
            if (this != EQUALITY && !(one instanceof Comparable)) {
                throw new IllegalArgumentException("Cannot relatively compare a non-comparable class " + one.getClass().getSimpleName());
            }
            return this.compare.test(one, two);
        }
    }

    public class Comparison<E> {

        private final DataField<E> field;
        private final E value;
        private final ComparisonType type;

        public Comparison(DataField<E> field, E value, ComparisonType type) {
            this.field = field;
            this.value = value;
            this.type = type;
        }

        public DataField<E> getDataField() {
            return this.field;
        }
        public E getValue() {
            return this.value;
        }
        public ComparisonType getType() {
            return this.type;
        }

        public boolean test(State s) {
            return this.getType().compare(this.getDataField().get(s), this.value);
        }

        //checks for states if it's possible to find them through our cache with 100% certainly on the result
        //otherwise, an uncertain result returns null, indicating the cache should be skipped
        public Stream<S> findStates() {
            if (this.getDataField().isKey()
                && this.getDataField().isUnique()
                && this.getType() == ComparisonType.EQUALITY) {
                //would fail if passed an unrelated data field
                //TODO: We should probably find a way to define a state->field association
                //TODO:     However, I want to avoid over-generifying
                return (Stream<S>) this.getDataField().findStatesByValue(StateQuery.this.storage, this.getValue());
            }
            //TODO: Basically, return anything indexed
            return null;
        }

        public String valueToString() {
            return Objects.toString(this.value);
        }

        public String toSQLPredicate() {
            return this.field.getName() + ' ' + this.getType().getOperator() + ' ' + this.valueToString();
        }

        public String toPreparedSQL() {
            return this.field.getName() + ' ' + this.getType().getOperator() + " ?";
        }

        public boolean isPreparable() {
            return true;
        }

    }

    public class MultiComparison<E> extends Comparison<E> {

        private final Set<E> values;

        public MultiComparison(DataField<E> field, Iterable<E> values) {
            super(field, null, ComparisonType.IS_ANY);
            this.values = StreamSupport.stream(values.spliterator(), false).collect(Collectors.toSet());
        }

        @Override
        public boolean test(State s) {
            E val = this.getDataField().get(s);
            return this.values.contains(val);
        }

        @Override
        public String toPreparedSQL() {
            return super.toSQLPredicate(); //aka don't add to prepared statement, unsupported
        }

        @Override
        public String valueToString() {
            return "(" + this.values.stream().map(Object::toString).collect(Collectors.joining(",")) + ")";
        }

        @Override
        public boolean isPreparable() {
            return false;
        }
    }

    Stream<S> locateLocal(StateStorage<S> storage) {
        Stream<S> cached = this.comparisons.stream()
                .map(Comparison::findStates)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        if (cached != null) {
            return cached;
        }
        //REFACTOR: old code below, can be made faster by using keys properly etc
        return storage.streamLoaded()
                .filter(s -> this.getComparisons().stream().allMatch(c -> c.test(s)));
    }

    Stream<S> runSQLQuery(SQLBiFunction<String, Object[], Stream<S>> create) throws SQLException {
        //yada yada sql injection
        //at the very least, we're only trusting ourselves.
        //I suppose even that could be dangerous, but still
        Class<? extends State> type = this.getType();
        State.Type stateType = StateType.fromClass(type);
        StorageContainer container = type.getAnnotation(StorageContainer.class);
        if (container == null) {
            throw new IllegalArgumentException("Unknown container for type: " + type.getSimpleName());
        }
        //REFACTOR: Think of a less hacky way to build an sql query here?
        String sqlWhere = this.getComparisons().stream()
                .map(Comparison::toPreparedSQL)
                .collect(Collectors.joining());
        Object[] args = this.getComparisons().stream()
                .filter(Comparison::isPreparable)
                .toArray(Object[]::new);
        return create.apply("SELECT * FROM " + container.value() + " WHERE " + sqlWhere, args);
    }
}
