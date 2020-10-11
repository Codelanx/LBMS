package edu.rit.codelanx.cmd.text;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TextParam {

    private final String label;
    private final int required; //minimum required args for list of args, <0 to disable
    private final boolean list; //if input is a list of arguments

    private TextParam(String label, int required, boolean list) {
        if (required < 0) {
            throw new IllegalArgumentException("Cannot require less than 0 arguments");
        }
        this.label = label;
        this.required = required;
        this.list = list;
    }

    @Override
    public String toString() {
        return this.list
                ? TextParam.joinList(this.label, this.required)
                : (this.required == 1 ? this.label : "[" + this.label + "]");
    }

    private static String joinList(String label, int num) {
        if (num < 0) {
            return label;
        } else if (num == 0) {
            return label + "...";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append(label).append(TextCommand.TOKEN_DELIMITER);
        }
        return sb.append("...").toString();
    }

    public enum Type {
        REQUIRED,
        OPTIONAL,
        DEFAULT,
        ;

        public String wrap(String input) {
            switch (this) {
                case REQUIRED:
                    return input;
                    //return '<' + input + '>'; //I believe we'll roll with this
                case OPTIONAL:
                    return '[' + input + ']';
            }
            return input;
        }
    }

    public static Builder create()  {
        return new Builder();
    }

    public static class Builder {

        private final List<TextParam> params = new LinkedList<>();

        public Builder listOptional(String label) {
            return this.list(label, 0);
        }

        public Builder list(String label, int required) {
            this.params.add(new TextParam(label, required, true));
            return this;
        }

        public Builder argumentOptional(String label) {
            return this.argument(label, false);
        }

        public Builder argument(String label) {
            return this.argument(label, true);
        }

        public Builder argument(String label, boolean required) {
            this.params.add(new TextParam(label, required ? 1 : 0, false));
            return this;
        }

        public String buildString() {
            return this.params.stream().map(TextParam::toString).collect(Collectors.joining(TextCommand.TOKEN_DELIMITER));
        }

        public TextParam[] build() {
            return this.params.toArray(new TextParam[0]);
        }
    }
}
