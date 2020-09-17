package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.cmd.Response;

public class TextResponse implements Response {

    public static final TextResponse ERROR = new TextResponse("Error processing command!");
    private final String data;

    public TextResponse(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }
}
