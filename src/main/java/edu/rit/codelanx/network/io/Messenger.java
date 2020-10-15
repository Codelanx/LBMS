package edu.rit.codelanx.network.io;

/**
 * Designates a class which can send or receive {@link Message Message&lt;M&gt;}
 * objects with other {@link Messenger Messenger&lt;M&gt;}'s of the same type
 *
 * @param <M> The {@link Message Message&lt;M&gt;} type this class expects
 * @author sja9291  Spencer Alderman
 * @see Message
 */
public interface Messenger<M extends Message<?>> {

    /**
     * Receiving a message from an external {@link Messenger Messenger&lt;M&gt;}
     *
     * @param from The {@link Messenger Messenger&lt;M&gt;} who sent the
     *             {@code message}
     * @param message The received {@link Message Message&lt;M&gt;}
     */
    public void receive(Messenger<M> from, M message);

    /**
     * Sends a given {@link Message Message&lt;M&gt;} to the provided
     * {@link Messenger Messenger&lt;M&gt;}. This is in effect equivalent to:
     *      {@code other.receive(this, message)}
     *
     * @param other The {@link Messenger} to receive the message
     * @param message The {@link Message} to be sent
     * @see #receive(Messenger, Message)
     */
    default public void sendTo(Messenger<M> other, M message) {
        other.receive(this, message);
    }
}
