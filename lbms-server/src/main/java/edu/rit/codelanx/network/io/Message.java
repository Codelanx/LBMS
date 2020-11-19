package edu.rit.codelanx.network.io;

/**
 * Interface for sending data between {@link Messenger} objects
 *
 * @param <T> The type of the data being sent
 * @author sja9291  Spencer Alderman
 */
public interface Message<T> {

    /**
     * The data contained in this {@link Message}
     *
     * @return The relevantly-typed data
     */
    public T getData();
}
