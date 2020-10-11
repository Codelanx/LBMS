package edu.rit.codelanx.util;

import java.io.PrintStream;

public enum Errors {;

    public static void report(Throwable t) {
        //no different than the generated code at the moment,
        //but will let us be nicer with the output of errors
        Errors.report(t, System.err);
    }

    public static void report(Throwable t, PrintStream out) {
        //no different than the generated code at the moment,
        //but will let us be nicer with the output of errors
        t.printStackTrace(out);
    }

    public static void reportAndExit(Throwable t) throws RuntimeException {
        t.printStackTrace();
        System.exit(1); //still runs shutdown hooks, vital for us to save stuff
        throw new RuntimeException("Unrecoverable condition", t); //We should absolutely kill the current thread though
    }
}
