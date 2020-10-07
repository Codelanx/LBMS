package edu.rit.codelanx.network.io;

/**
 * Provides an interface representing a messenger. This can be either internal system or a client
 * @param <M>-type of Message being exchanged
 * @author sja9291  Spencer Alderman
 */
public interface Messenger<M extends Message<?>> {
    /**
     * gets the message and process it
     * @param from-The {@link Messenger} we received a message from
     * @param message-The received {@link Message}
     */
    public void receive(Messenger<M> from, M message);

    /**
     * sends the message to a particular messenger
     * @param other- the {@link Messenger} to receive the message
     * @param message- the {@link Message} to be sent
     */
    default public void message(Messenger<M> other, M message) {
        other.receive(this, message);
    }
}
