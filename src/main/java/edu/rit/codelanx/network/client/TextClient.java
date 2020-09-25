package edu.rit.codelanx.network.client;

import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.network.io.Message;
import edu.rit.codelanx.network.io.Messenger;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ref.WeakReference;


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
        //TODO: Consume main thread
        String s = null;
        while ((s = this.buffer.readLine()) != null) {
            Server<TextMessage> server = this.server.get();
            if (server == null) {
                System.err.println("Not connected to server!"); //prints per input attempt
                continue;
            }
            /*server.getReceiver().receive(this, new TextMessage(s));
            this.message(server.getReceiver(), new TextMessage(s));
            this.message(server.getMessenger(), new TextMessage(s));*/
        }
    }

    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void renderState(State... state) {
        //think about how to render Book, Visitor, etc
    }

    @Override
    public void receive(Messenger<TextMessage> from, TextMessage message) {
        if (!(from instanceof Server)) { //extra safety check, necessary?
            throw new UnsupportedOperationException("(Shouldn't) support messaging from non-servers");
        }
        this.output.println(message.getData());
    }


    //    @Override
//    public void close() throws Exception {
//        this.buffer.close();
//        this.reader.close();
//    }

//    @Override
//    public void display() {
//        try {
//            String rq= readInput(buffer);
//            TextRequest request= new TextRequest(rq);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //TODO: revise
//
//    }
//



}
