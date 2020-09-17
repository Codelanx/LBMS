package edu.rit.codelanx.data;

import java.util.stream.Stream;

public interface DataStorage {

    /**
     * Returns all of the stored data relevant to the given {@link Class type}.
     *
     * @param type The type of {@link State} to retrieve
     * @param <R> The generic type witness to {@code type}
     * @return A {@link Stream Stream<R>} of all possible, loaded values.
     */
    public <R extends State> Stream<? extends R> ofLoaded(Class<R> type);

    public void add(State state);
}
