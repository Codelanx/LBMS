package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.cmd.Request;

public class TextRequest implements Request {

    private final String data;

    public TextRequest(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }
}
