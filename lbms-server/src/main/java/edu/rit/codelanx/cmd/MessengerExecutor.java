package edu.rit.codelanx.cmd;

import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;

import java.util.Objects;

public class MessengerExecutor implements CommandExecutor {

    private final Messenger<TextMessage> receiver;
    private final Messenger<TextMessage> messenger;
    private final StringBuilder buffer = new StringBuilder();

    public MessengerExecutor(Messenger<TextMessage> receiver, Messenger<TextMessage> executor) {
        this.receiver = receiver;
        this.messenger = executor;
    }

    @Override
    public void sendMessage(String message) {
        if (this.buffer.length() > 0) {
            this.buffer.append('\n');
        }
        this.buffer.append(message);
        this.receiver.sendTo(this.messenger, new TextMessage(message));
    }

    @Override
    public boolean flush() {
        if (this.buffer.length() == 0) return false;
        String s = this.buffer.toString();
        this.buffer.setLength(0);
        this.receiver.sendTo(this.messenger, new TextMessage(s));
        return true;
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
