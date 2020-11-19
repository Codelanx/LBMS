package edu.rit.codelanx.data.storage;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.field.DataField;

/**
 * A "builder" class for loading a {@link State} from one storage to another.
 * This class actually takes advantage of the {@link StateBuilder}'s ability
 * to be inserted to a {@link DataSource}
 *
 * @param <T> The {@link State} to load into a new {@link DataSource}
 */
public class ProxiedStateBuilder<T extends State> extends StateBuilder<T> {

    private final T other;

    public ProxiedStateBuilder(T other) {
        super(other.getType(), other.getIDField(), other.getFields());
        this.other = other;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <E> E getValue(DataField<E> field) {
        E override = super.getValue(field);
        return override == null && field != other.getIDField()
                ? field.get(this.other)
                : override;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected T buildObj(DataSource storage, long id) {
        if (storage == this.other.getLoader()) {
            throw new UnsupportedOperationException("Cannot build a proxied state on the same data source");
        }
        //behold the power of type witnessing!
        return this.other.getType().<T>getBuilderConstructor().create(storage, id, this);
    }
}
