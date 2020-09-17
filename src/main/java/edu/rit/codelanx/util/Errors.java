package edu.rit.codelanx.util;

public class Errors {

    public static void report(Throwable t) {
        //no different than the generated code at the moment,
        //but will let us be nicer with the output of errors
        t.printStackTrace();
    }
}
