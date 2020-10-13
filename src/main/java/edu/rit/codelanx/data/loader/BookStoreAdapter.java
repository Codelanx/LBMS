package edu.rit.codelanx.data.loader;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Author;
import edu.rit.codelanx.data.state.types.AuthorListing;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.util.BookParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class BookStoreAdapter extends FFStorageAdapter {

    private static Set<Class<? extends State>> KNOWN_TYPES;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    static {
        KNOWN_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Author.class, AuthorListing.class, Book.class)));
    }

    public BookStoreAdapter(DataSource storage) {
        super(storage, "json"); //TODO: this is invalid, needs a proper valuue / fix for the parent
    }

    @Override
    public Library getLibrary() {
        throw new UnsupportedOperationException("Book store knows nothing about libraries");
    }

    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        if (this.initialized.get()) {
            throw new UnsupportedOperationException("Book store does not support adding new states");
        }
        return super.insert(builder);
    }

    @Override
    public void loadAll() throws IOException {
        BookParser.parseBooks(this.getAdaptee());
    }

    @Override
    public void saveAll() throws IOException {
        //no-op: file is always available
    }

    @Override
    public <R extends State> Stream<R> handleQuery(StateQuery<R> query) {
        if (!KNOWN_TYPES.contains(query.getType())) {
            throw new IllegalArgumentException("Book Store knows nothings about " + query.getType());
        }
        return super.handleQuery(query);
    }

    @Override
    public <R extends State> R loadState(long id, Class<R> type) {
        if (!KNOWN_TYPES.contains(type)) {
            throw new IllegalArgumentException("Book Store knows nothings about " + type);
        }
        return super.loadState(id, type);
    }

    @Override
    public <R extends State, E> Stream<R> loadState(Class<R> type, DataField<E> field, E value) {
        if (!KNOWN_TYPES.contains(type)) {
            throw new IllegalArgumentException("Book Store knows nothings about " + type);
        }
        return super.loadState(type, field, value);
    }

    @Override
    public <E> void notifyUpdate(State state, DataField<E> field, E value) {
        throw new UnsupportedOperationException("Cannot update values in a remote service (The book store)");
    }

    @Override
    public void remove(State state) {
        throw new UnsupportedOperationException("Cannot update values in a remote service (The book store)");
    }

}
