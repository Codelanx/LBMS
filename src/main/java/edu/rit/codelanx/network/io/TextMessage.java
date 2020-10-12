package edu.rit.codelanx.network.io;

/**
 * A concrete type for {@link Message Message<String>}, as trying to pass
 * around generified types for a {@link Messenger} results in untenable generics
 * and unreadable code. Instead, a generic could simply reference this class
 * when being instantiated, and save a lot of type-casting headaches
 *
 * @author sja9291  Spencer Alderman
 */
public class TextMessage implements Message<String> {

    //the data held in this message
    private final String data;

    /**
     * Sets the value of this message with the provide {@code data}
     *
     * @param data The {@link String} to send in this message
     */
    public TextMessage(String data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     * @return The {@link String} that was sent
     */
    @Override
    public String getData() {
        return this.data;
    }
}
