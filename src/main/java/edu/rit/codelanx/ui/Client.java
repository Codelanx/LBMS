package edu.rit.codelanx.ui;

import edu.rit.codelanx.data.state.State;

import java.io.BufferedReader;
import java.io.IOException;

public abstract class Client implements AutoCloseable{
    private IDisplay IDisplay;
    private IReadInput IReadInput;
    private IMessage IMessage;

    Client (IReadInput readInput, IDisplay display, IMessage message){
        this.IDisplay= display;
        this.IReadInput= readInput;
        this.IMessage= message;
    }

    public void display(){
        this.IDisplay.display();
    }

    public String readInput(BufferedReader buffer) throws IOException {
        return this.IReadInput.readInput(buffer);
    }

    public void sendMessage(String message){
        this.IMessage.send(message);
    }

    public void renderState(State state) {
    }

}
