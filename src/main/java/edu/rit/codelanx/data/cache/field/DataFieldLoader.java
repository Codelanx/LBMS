package edu.rit.codelanx.data.cache.field;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.loader.InputMapper;
import edu.rit.codelanx.data.state.State;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class DataFieldLoader<T> implements DataField<T> {

    protected final Map<State, T> values = new WeakHashMap<>();
    private final DataField<?> proxied;
    private final FieldInitializer<T> initializer;

    public DataFieldLoader(FieldInitializer<T> initializer) {
        this(null, initializer);
    }

    public DataFieldLoader(DataField<?> proxied, FieldInitializer<T> initializer) {
        if (proxied != null && !InputMapper.isStateClass(initializer.getType())) {
            throw new IllegalArgumentException("Cannot map a concrete field");
        }
        this.proxied = proxied;
        this.initializer = initializer;
    }

    /**
     * {@inheritDoc}
     * @return string representation of {@link FieldInitializer}
     */
    @Override
    public String getName() {
        return this.initializer.getName();
    }

    @Override
    public Object serialize(State state) {
        //If we're proxied, then we're mapping a State inherently
        return this.proxied != null
                ? this.proxied.serialize((State) this.get(state))
                : this.get(state);
    }

    @Override
    @SuppressWarnings("unchecked") //in this case, (T) will fast fail
    public void initialize(State state, Object value) {
        if (value.getClass() != this.getType()) {
            throw new ClassCastException("Unsupported type: " + value.getClass());
        }
        T val = (T) value;
        if (this.values.putIfAbsent(state, val) != null) {
            throw new UnsupportedOperationException("Cannot re-initialize a value");
        }
    }

    /**
     * {@inheritDoc}
     * @param state {@link State} to be cleared out.
     */
    @Override
    public void forget(State state) {
        this.values.remove(state);
    }

    @Override
    public T mutate(State state, UnaryOperator<T> updater) {
        T next = this.values.compute(state, (k, old) -> updater.apply(old));
        this.notify(state, next);
        return next;
    }

    @Override
    public T get(State state) {
        T back = this.values.get(state);
        if (back == null) { //TODO: Need to allow nulls and checking if defaults for states
            back = this.getInitializer().getDefaultValue();
        }
        return back;
    }

    @Override
    public T set(State state, T value) {
        T old = this.values.put(state, value);
        this.notify(state, value);
        return old;
    }

    protected void notify(State state, T newValue) {
        state.getLoader().getAdapter().notifyUpdate(state, this, newValue);
    }

    /**
     * {@inheritDoc}
     * @return involved {@link FieldInitializer}
     */
    @Override
    public FieldInitializer<T> getInitializer() {
        return this.initializer;
    }

    //no handling on the loader basis
    @Override
    public Stream<? extends State> findStatesByValue(DataSource source, T key) {
        return this.findStatesByValue(key);
    }

    @Override
    public Stream<? extends State> findStatesByValue(T key) {
        //ignore: source, only knows of "one" data source
        //Do a slow search if we can't do by key
        Predicate<T> check = key == null ? Objects::isNull : key::equals;
        return this.values.entrySet().stream()
                .filter(ent -> check.test(ent.getValue()))
                .map(Map.Entry::getKey);
    }
}
