package edu.rit.codelanx.data.loader;

import edu.rit.codelanx.data.cache.field.DataField;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Query<T> {

    //runtime
    public <E> Query<T> filterBy(DataField<E> field, Predicate<E> test);
    public Query<T> local();
    public Query<T> remote();
    public Query<T> isID(long id);

    //comparisons
    public <E> Query<T> isEqual(DataField<E> field, E value);
    public <E> Query<T> isAny(DataField<E> field, Iterable<E> value);
    public <E extends Comparable<E>> Query<T> isLessThan(DataField<E> field, E value);
    public <E extends Comparable<E>> Query<T> isGreaterThan(DataField<E> field, E value);
    public <E extends Comparable<E>> Query<T> isLessThanOrEq(DataField<E> field, E value);
    public <E extends Comparable<E>> Query<T> isGreaterThanOrEq(DataField<E> field, E value);

    default public <E> Query<T> isAny(DataField<E> field, E... value) {
        return this.isAny(field, Arrays.asList(value));
    }

    public Stream<T> results();

}
