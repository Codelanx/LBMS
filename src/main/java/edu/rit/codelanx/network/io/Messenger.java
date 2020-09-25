package edu.rit.codelanx.network.io;

public interface Messenger<M extends Message<?>> {

    public void receive(Messenger<M> from, M message);

    default public void message(Messenger<M> other, M message) {
        other.receive(this, message);
    }
}
