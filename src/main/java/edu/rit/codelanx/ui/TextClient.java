package edu.rit.codelanx.ui;

import edu.rit.codelanx.Server;
//import edu.rit.codelanx.data.State;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.types.*;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedHashMap;
import java.util.Locale;


public class TextClient implements ITextClient{
    private InputStreamReader reader;
    private BufferedReader buffer;
    private PrintStream output;
    private Server server;

    public TextClient(InputStream input, PrintStream output) {
        this.reader = new InputStreamReader(input);
        this.buffer = new BufferedReader(this.reader);
        this.output= output;
    }

    /**
     * reads from buffered reader then sends request to the server
     * @throws IOException
     */
    @Override
    public void display() throws IOException {
        String str;
        while ((str = buffer.readLine()) != null) {
            server.receive(this, new TextMessage(str));
        }
    }
    @Override
    public void connect(Server server) {
        server.registerClient(this);
        this.server=server;
    }
    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
    }
    /**
     * renders infos of the specified state.
     * @param state
     */
    @Override
    public void renderState(State state) {
        String s;
        String formatted_s=null;
        if (state instanceof Visitor){
            Visitor visitor= (Visitor) state;
            s="Visitor ID:%d| First Name: %s| Last name:%s |Address: %s| phone: %d| Currently visit:%b|balance amount= %d";
            formatted_s= String.format(s, visitor.getID(),visitor.getFirstName(),visitor.getLastName(), visitor.getAddress(), visitor.getPhone(), visitor.isVisiting(), visitor.getMoney());
        }else if(state instanceof Visit){
            Visit visit= (Visit) state;
            s="Visitor ID: %d| Arrival Time:%s | Departure time:%s";
            formatted_s= String.format(s, visit.getID(), formatTime(visit.getStart()), formatTime(visit.getEnd()));
        }else if(state instanceof Transaction) {
            Transaction transaction= (Transaction) state;
            s= "Visitor ID: %d | transaction amount: %d";
            formatted_s= String.format(s, transaction.getVisitorID(), transaction.getAmount());
        }else if(state instanceof Library) {
            Library lib= (Library) state;
            s= "Library Currently opens: %b";
            formatted_s= String.format(s, lib.isOpen());
        }else if(state instanceof Checkout) {
            Checkout checkout= (Checkout) state;
            s= "Visitor ID: %d | Book id: %d| checkout time: %s";
            formatted_s= String.format(s, checkout.getVisitorID(), checkout.getBookID(), formatTime(checkout.getBorrowedAt()));
        }else if(state instanceof Book) {
            Book book= (Book) state;
            s= "Title: %s | ISBN: %d| Author: %s| Publisher: %s| publish date:%s| Total pages: %d| Total copies: %d| Total checkout: %d";
            formatted_s= String.format(s, book.getTitle(), book.getISBN(), book.getAuthor(), book.getPublisher(),
                    book.getPublishDate(), book.getPageCount(), book.getTotalCopies(), book.getCheckedOut());
        }
        this.output.println(formatted_s);

    }

    /**
     * format time (type instant) into a string
     * @param time
     * @return
     */
    private String formatTime(Instant time){
        DateTimeFormatter formatter= DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.US).withZone(ZoneId.systemDefault());
        String str_time=formatter.format(time);
        return str_time;
    }

    /**
     * receives message from the server.
     * @param server
     * @param message
     */
    @Override
    public void receive(Server server, TextMessage message){
        this.output.println(message.getData());
    }



}
