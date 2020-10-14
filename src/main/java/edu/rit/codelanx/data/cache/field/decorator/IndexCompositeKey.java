package edu.rit.codelanx.data.cache.field.decorator;

import edu.rit.codelanx.data.loader.Query;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.cache.field.FieldIndicies;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class IndexCompositeKey<T> extends FieldIndex<T> {

    private final List<IndexCompositeKey<?>> linkedKeys = new ArrayList<>();

    public IndexCompositeKey(DataField<T> parent) {
        super(parent);
    }

    /**
     * Associates two {@link IndexCompositeKey} objects to reference each other
     *
     * {@inheritDoc}
     * param other {@inheritDoc}
     */
    /*@Override
    public void compose(DataField<?>... others) {
        for (DataField<?> field : others) {
            if (field == this) continue;
            if (!field.isComposite()) {
                throw new IllegalArgumentException("Cannot build a composite key with a non-composite partner: " + field.getName());
            }
            IndexCompositeKey<?> o = (IndexCompositeKey<?>) field;
            this.linkedKeys.add(o);
            o.linkedKeys.add(this);
        }
        if (!other.isComposite()) {
            throw new IllegalArgumentException("Cannot build a composite key with a non-composite partner: " + other.getName());
        }
        IndexCompositeKey<?> o = (IndexCompositeKey<?>) other;
        this.linkedKeys.add(o);
        o.linkedKeys.add(this);
    }

    public static void applyFor(DataField<?>... fields) {
        for (DataField<?> field : fields) {
            if (!field.isComposite()) {
                throw new IllegalArgumentException("Cannot build a composite key with a non-composite partner: " + field.getName());
            }
        }
    }*/

    @Override
    public void initialize(State state, T value) {
        super.initialize(state, value);
    }

    @Override
    public FieldIndicies getIndexType() {
        return null;
    }

    @Override
    public T set(State state, T value) {
        Query<? extends State> query = state.getLoader().query(state.getType().getConcreteType());
        for (DataField<?> f : this.linkedKeys) {
            Object attempt = f.getInitializer() == this.getInitializer() ? value : f.get(state);
            query = query.isEqual((DataField<Object>) f, attempt);
        }
        if (query.results().count() > 1) {
            String matching = String.join(",", this.linkedKeys.stream().map(DataField::getName).toArray(String[]::new));
            matching = "{" + matching + "}";
            throw new IllegalArgumentException("Cannot update " + this.getName() + ": violates composite key over " + matching);
        }
        return super.set(state, value);
    }

    @Override
    public T mutate(State state, UnaryOperator<T> updater) {
        Query<? extends State> query = state.getLoader().query(state.getType().getConcreteType());
        T mapped = updater.apply(this.get(state));
        for (DataField<?> f : this.linkedKeys) {
            if (f.getInitializer() == this.getInitializer()) continue;
            Object value = f.get(state);
            query = query.isEqual((DataField<Object>) f, value);
        }
        if (query.results().count() > 1) {
            String matching = String.join(",", this.linkedKeys.stream().map(DataField::getName).toArray(String[]::new));
            matching = "{" + matching + "}";
            throw new IllegalArgumentException("Cannot update field " + this.getName()
                    + " in " + state.getType().getName()
                    + ": violates composite key over " + matching);
        }
        this.set(state, mapped);
        return mapped;
    }

    //TODO: Cover in DataField


    /**
     * Links two indicies together, such that operations on one are dependant
     * upon the other, and vice-versa. This is primarily utilized to support
     * composite key functionality. This method may not do anything if either
     * argument is not a composite key. Additionally, due to the nature
     * of composition, you may compose a key to itself (it won't really do
     * anything, however)
     *
     * @param others The other {@link DataField}s to link to
     * @see edu.rit.codelanx.data.cache.field.decorator.IndexCompositeKey
     * @see #isComposite()
     */
    //public void compose(DataField<?>... others);

    /**
     * Returns whether this field is a composite key, meaning it shares in a
     * key or behavioral contract across multiple data fields
     *
     * @return {@code true} if this field is a composite key
     */
    /*default public boolean isComposite() {
        return false;
    }*/
}
