package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.util.Validate;

import java.util.Arrays;
import java.util.Objects;

public class MessengerExecutor implements CommandExecutor {

    private final Messenger<TextMessage> receiver;
    private final Messenger<TextMessage> messenger;

    public MessengerExecutor(Messenger<TextMessage> receiver, Messenger<TextMessage> executor) {
        this.receiver = receiver;
        this.messenger = executor;
    }

    @Override
    public void sendMessage(String message) {
        this.receiver.sendTo(this.messenger, new TextMessage(message));
    }

    @Override
    public void renderState(State... states) {
        Arrays.stream(states)
                .peek(s -> Validate.nonNull(s, "Cannot render a null state"))
                .map(State::toFormattedText)
                .forEach(this::sendMessage); //final code version
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessengerExecutor)) return false;
        MessengerExecutor that = (MessengerExecutor) o;
        return Objects.equals(receiver, that.receiver) &&
                Objects.equals(messenger, that.messenger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiver, messenger);
    }
}
