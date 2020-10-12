package edu.rit.codelanx.data.state.types;

import com.codelanx.commons.data.SQLBiFunction;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.loader.InputMapper;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.State;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

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
    private final AtomicLong autoID = new AtomicLong(1);
    private final Class<? extends State> type;
    private final StateBuilder.StateConstructor<?> builderBlueprint;
    private final SQLBiFunction<DataStorage, ResultSet, ? extends State> sqlBuild;
    private final BiFunction<DataStorage, Map<String, Object>, ? extends State> fileBuild;

    private <T extends State> StateType(Class<T> type,
                 StateBuilder.StateConstructor<T> blueprint,
                 SQLBiFunction<DataStorage, ResultSet, T> sqlBuild,
                 BiFunction<DataStorage, Map<String, Object>, T> fileBuild) {
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
    //warnings not suppressed here, this is actually a little dangerous
    @Override
    public <T extends State> Class<T> getConcreteType() {
        return (Class<T>) this.type; //will immediately CCE if invalidly used
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public State mapFromSQL(DataStorage storage, ResultSet set) throws SQLException {
        return this.sqlBuild.apply(storage, set);
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public State mapFromFile(DataStorage storage, Map<String, Object> file) {
        return this.fileBuild.apply(storage, file);
    }

    public static State.Type fromClass(Class<? extends State> stateType) {
        for (StateType t : VALUES) {
            if (t.type == stateType) {
                return t;
            }
        }
        return null;
    }

    public static State.Type fromClassStrict(Class<?> type) {
        State.Type back = null;
        if (InputMapper.isStateClass(type)) {
            back = StateType.fromClass((Class<? extends State>) type);
        }
        if (back == null) {
            throw new IllegalArgumentException("Cannot locate type for state: " + type.getName());
        }
        return back;
    }
}
