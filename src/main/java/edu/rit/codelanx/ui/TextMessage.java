package edu.rit.codelanx.ui;

public class TextMessage implements IMessage {
    private String message;
    TextMessage (String message){
        this.message= message;
    }
    @Override
    public String getData() {
        return message;
    }
}
