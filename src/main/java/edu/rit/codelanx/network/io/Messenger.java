package edu.rit.codelanx.network.io;

/**
 * Designates a class which can send or receive {@link Message Message<T>}
 * objects with other {@link Messenger Messenger<T>}'s of the same type
 *
 * @param <M> The {@link Message Message<T>} type this class expects
 * @author sja9291  Spencer Alderman
 * @see Message
 */
public interface Messenger<M extends Message<?>> {

    /**
     * Receiving a message from an external {@link Messenger Messenger<T>}
     *
     * @param from The {@link Messenger Messenger<T>} who sent the
     *             {@code message}
     * @param message The received {@link Message Message<T>}
     */
    public void receive(Messenger<M> from, M message);

    /**
     * Sends a given {@link Message Message<T>} to the provided
     * {@link Messenger Messenger<T>}. This is in effect equivalent to:
     *      {@code other.receive(this, message)}
     *
     * @param other The {@link Messenger} to receive the message
     * @param message The {@link Message} to be sent
     * @see #receive(Messenger, Message)
     */
    default public void message(Messenger<M> other, M message) {
        other.receive(this, message);
    }
}
