package edu.rit.codelanx.data.cache;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.field.DataField;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

//TODO: Remove in future release, was an unintended leave-in
//stores hard references for states, where they won't go out of scope (unless we want them to)
@Deprecated
public class StateStorage<T extends State> {

    private final Map<Long, T> loaded = new WeakHashMap<>();
    private final State.Type type;
    private final Class<T> concreteType;
    private final DataSource storage;

    public StateStorage(State.Type type, DataSource storage) {
        this.type = type;
        this.concreteType = this.type.getConcreteType();
        this.storage = storage;
    }

    public T getByID(long id) {
        return this.loaded.computeIfAbsent(id, k -> {
            return this.storage.getAdapter().loadState(k, this.concreteType);
        });
    }

    public T computeIfAbsent(long id, Supplier<T> state) {
        return this.loaded.computeIfAbsent(id, k -> state.get());
    }

    // We quite literally do a check for T
    @Deprecated
    public <E> T getUniqueValue(DataField<E> field, E value) {
        if (!field.isUnique()) {
            throw new IllegalStateException("Cannot retrieve a unique value from a non-keyed field");
        }
        if (field.isKey()) {
            T s = (T) field.findStatesByValue(this.storage, value).findAny().orElse(null);
            if (s == null) {
                //not loaded?
                s = this.storage.getAdapter().loadState(this.concreteType, field, value).findAny().orElse(null);
                if (s != null && this.concreteType.isAssignableFrom(s.getClass())) {
                    //found by indexing, whew
                    this.loaded.put(s.getID(), (T) s);
                    return (T) s;
                }
            } else if (!this.concreteType.isAssignableFrom(s.getClass())) {
                throw new IllegalArgumentException("Wrong state type returned from index search");
            }
        }
        //searches everything else - slower if not indexed
        Predicate<E> test = value == null ? Objects::isNull : value::equals;
        return this.loaded.values().stream()
                .filter(s ->  test.test(field.get(s)))
                .findAny().orElseGet(() -> {
                    T back = this.storage.query(this.concreteType)
                            .isEqual(field, value)
                            .results().findAny().orElse(null);
                    if (back == null) {
                        return null;
                    }
                    this.loaded.put(back.getID(), back);
                    return back;
                });

    }

    public Stream<T> streamLoaded() {
        return this.loaded.values().stream();
    }

    public void addState(T state) {
        this.loaded.put(state.getID(), state);
    }

    public void release(T state) {
        if (this.storage.getAdapter().isCached()) {
            return;
        }
        this.doRelease(state);
    }

    private void doRelease(T state) {
        this.loaded.remove(state.getID());
        Arrays.stream(state.getFields()).forEach(d -> d.forget(state));
    }

    public void remove(T state) {
        this.storage.getAdapter().remove(state);
        this.doRelease(state); //REFACTOR: potential race condition
    }

    public void forAllLoaded(Consumer<T> consumer) {
        this.loaded.values().forEach(consumer);
    }
}
