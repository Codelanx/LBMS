package edu.rit.codelanx.ui;

import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextRequest;
import edu.rit.codelanx.cmd.text.TextResponse;
//import edu.rit.codelanx.data.State;
import edu.rit.codelanx.data.state.State;

import java.io.*;


public class TextClient extends Client implements AutoCloseable{
    private InputStreamReader reader;
    private BufferedReader buffer;

    public TextClient(InputStream input, PrintStream output) {
        super(new ReadTextInput(input, output),new TextDisplay(),new TextMessage());
        this.reader = new InputStreamReader(input);
        this.buffer = new BufferedReader(this.reader);
    }

    @Override
    public void close() throws Exception {
        this.buffer.close();
        this.reader.close();
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
