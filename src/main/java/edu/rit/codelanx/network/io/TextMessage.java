package edu.rit.codelanx.network.io;

//TextResponse
//TextRequest
public class TextMessage implements Message<String> {

    private final String data;

    public TextMessage(String data) {
        this.data = data;
    }

    @Override
    public String getData() {
        return this.data;
    }
}
