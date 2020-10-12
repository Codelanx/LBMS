package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.loader.InputMapper;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.cache.field.DataField;

import java.util.concurrent.atomic.AtomicLong;

public enum StateType implements State.Type {
    BOOK(Book.class, Book::new, Book::new, Book::new),
    CHECKOUT(Checkout.class, Checkout::new, Checkout::new, Checkout::new),
    LIBRARY(Library.class, Library::new, Library::new, Library::new),
    TRANSACTION(Transaction.class, Transaction::new, Transaction::new, Transaction::new),
    VISIT(Visit.class, Visit::new, Visit::new, Visit::new),
    VISITOR(Visitor.class, Visitor::new, Visitor::new, Visitor::new),
    AUTHOR(Author.class, Author::new, Author::new, Author::new),
    AUTHOR_LISTING(AuthorListing.class, AuthorListing::new, AuthorListing::new, AuthorListing::new),
    //UNKNOWN is unimplemented
    ;

    private static final StateType[] VALUES = StateType.values();
    private final AtomicLong autoID = new AtomicLong(1000000001); //we start with 10-digit ids
    private final Class<? extends State> type;
    private final State.StateBuildConstructor<? extends State> builderBlueprint;
    private final State.StateSQLConstructor<? extends State> sqlBuild;
    private final State.StateFileConstructor<? extends State> fileBuild;

    private <T extends State> StateType(Class<T> type,
                 State.StateBuildConstructor<T> blueprint,
                 State.StateSQLConstructor<T> sqlBuild,
                 State.StateFileConstructor<T> fileBuild) {
        this.type = type;
        this.builderBlueprint = blueprint;
        this.sqlBuild = sqlBuild;
        this.fileBuild = fileBuild;
    }

    public void setAutoIncrementID(long value) {
        this.autoID.set(value);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public long getNextID() {
        return this.autoID.getAndIncrement();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name().toLowerCase();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    //To fix, we'd have to de-enum. Instead, this will CCE if invalid
    //Additionally we can forgo the casts externally, the warnings are all here
    @SuppressWarnings("unchecked")
    @Override
    public <T extends State> Class<T> getConcreteType() {
        return (Class<T>) this.type; //will immediately CCE if invalidly used
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    //To fix, we'd have to de-enum. Instead, this will CCE if invalid
    //Additionally we can forgo the casts externally, the warnings are all here
    @SuppressWarnings("unchecked")
    @Override
    public <T extends State> State.StateBuildConstructor<T> getBuilderConstructor() {
        return (State.StateBuildConstructor<T>) this.builderBlueprint;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    //To fix, we'd have to de-enum. Instead, this will CCE if invalid
    //Additionally we can forgo the casts externally, the warnings are all here
    @SuppressWarnings("unchecked")
    @Override
    public <T extends State> State.StateSQLConstructor<T> getSQLConstructor() {
        return (State.StateSQLConstructor<T>) this.sqlBuild;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    //To fix, we'd have to de-enum. Instead, this will CCE if invalid
    //Additionally we can forgo the casts externally, the warnings are all here
    @SuppressWarnings("unchecked")
    @Override
    public <T extends State> State.StateFileConstructor<T> getFileConstructor() {
        return (State.StateFileConstructor<T>) this.fileBuild;
    }

    public static State.Type fromClassNullable(Class<? extends State> stateType) {
        for (StateType t : VALUES) {
            if (t.type == stateType) {
                return t;
            }
        }
        return null;
    }

    public static <T extends State> State.Type fromClass(Class<T> type) {
        State.Type back = StateType.fromClassNullable(type);
        if (back == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return back;
    }
}
