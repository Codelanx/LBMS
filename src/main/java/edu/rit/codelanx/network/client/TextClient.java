package edu.rit.codelanx.network.client;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.types.Checkout;
import edu.rit.codelanx.data.types.Library;
import edu.rit.codelanx.data.types.Transaction;
import edu.rit.codelanx.data.types.Visit;
import edu.rit.codelanx.data.types.Visitor;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;


public class TextClient implements Client<TextMessage> {

    private final InputStreamReader reader;
    private final BufferedReader buffer;
    private final PrintStream output;
    private volatile WeakReference<Server<TextMessage>> server;
    /*
    public final class System {
    public static final InputStream in;
    public static final PrintStream out;
    public static final PrintStream err;
     */

    public TextClient(InputStream input, PrintStream output) {
        this.reader = new InputStreamReader(input);
        this.buffer = new BufferedReader(this.reader);
        this.output = output;
    }

    @Override
    public void connect(Server<TextMessage> server) {
        this.server = new WeakReference<>(server);
    }

    @Override
    public void display() throws IOException {
        String s;
        while ((s = this.buffer.readLine()) != null) {
            Server<TextMessage> server = this.server.get();
            if (server == null) {
                System.err.println("Not connected to server!"); //prints per input attempt
                continue;
            }
            this.message(server, new TextMessage(s));
        }
    }

    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
    }

    @Override
    public void sendMessage(String message) {
        this.output.println(message);
    }

    @Override
    public void renderState(State... states) {
        Arrays.stream(states)
                .map(State::toFormattedText)
                .forEach(this.output::println); //final code version

        String s;
        String formatted_s = state.toFormattedText();
        this.output.println(formatted_s);
        if (state instanceof Visitor) {
            Visitor visitor = (Visitor) state;
            s = "Visitor ID:%d| First Name: %s| Last name:%s |Address: %s| phone: %d| Currently visit:%b|balance amount= %d";
            formatted_s = String.format(s, visitor.getID(), visitor.getFirstName(), visitor.getLastName(), visitor.getAddress(), visitor.getPhone(), visitor.isVisiting(), visitor.getMoney());
        } else if (state instanceof Visit) {
            Visit visit = (Visit) state;
            s = "Visitor ID: %d| Arrival Time:%s | Departure time:%s";
            formatted_s = String.format(s, visit.getID(), formatTime(visit.getStart()), formatTime(visit.getEnd()));
        } else if (state instanceof Transaction) {
            Transaction transaction = (Transaction) state;
            s = "Visitor ID: %d | transaction amount: %d";
            formatted_s = String.format(s, transaction.getVisitorID(), transaction.getAmount());
        } else if (state instanceof Library) {
            Library lib = (Library) state;
            s = "Library Currently opens: %b";
            formatted_s = String.format(s, lib.isOpen());
        } else if (state instanceof Checkout) {
            Checkout checkout = (Checkout) state;
            s = "Visitor ID: %d | Book id: %d| checkout time: %s";
            formatted_s = String.format(s, checkout.getVisitorID(), checkout.getBookID(), formatTime(checkout.getBorrowedAt()));
        } else { /* ... */ } //Old: Book
    }

    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {
        if (!(from instanceof Server)) { //extra safety check, necessary?
            throw new UnsupportedOperationException("(Shouldn't) support messaging from non-servers");
        }
        this.output.println(message.getData());
    }

}
