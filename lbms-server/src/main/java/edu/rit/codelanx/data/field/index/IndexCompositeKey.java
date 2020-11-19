package edu.rit.codelanx.data.field.index;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.field.FieldIndicies;
import edu.rit.codelanx.data.storage.Query;
import edu.rit.codelanx.data.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * a {@link DataField} represent the Index Composite Key
 *
 * @param <T> of specified type
 * @author sja9291  Spencer Alderman
 */
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
    @Override
    public void initialize(State state, T value) {
        super.initialize(state, value);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public FieldIndicies getIndexType() {
        return null;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
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
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
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
     * @see IndexCompositeKey
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
