package edu.rit.codelanx.ui;

public class TextMessage implements IMessage {
    @Override
    public void send(String message) {
        System.out.println(message);
    }
}
