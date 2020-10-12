package edu.rit.codelanx.util;

import java.io.PrintStream;

/**
 * A utility class for handling and reporting errors throughout the code
 *
 * @author sja9291  Spencer Alderman
 */
public enum Errors {;

    /**
     * Prints a given error to {@link PrintStream stderr} for this system
     *
     * @param t The {@link Throwable} to print out
     */
    public static void report(Throwable t) {
        //no different than the generated code at the moment,
        //but will let us be nicer with the output of errors
        Errors.report(t, System.err);
    }

    /**
     * Offers a central point for handling how {@link Throwable errors} should
     * be displayed to a given {@link PrintStream}
     *
     * @param t The {@link Throwable} to display
     * @param out The {@link PrintStream} to display on
     */
    public static void report(Throwable t, PrintStream out) {
        //no different than the generated code at the moment,
        //but will let us be nicer with the output of errors
        t.printStackTrace(out);
    }

    /**
     * Reports a given error to {@link PrintStream stderr}, and exits
     * immediately. For good measure, this method will even throw a
     * {@link RuntimeException} to ensure the code flow is interrupted. This
     * call is effectively the same as:
     *      {@code Errors.reportAndExit("unrecoverable condition", t);}
     *
     * @param t The {2link Throwable} that caused this fatal crash
     * @throws RuntimeException A wrapper for the passed in {@link Throwable}
     * @see #reportAndExit(String, Throwable)
     */
    public static void reportAndExit(Throwable t) throws RuntimeException {
        t.printStackTrace();
        System.exit(1); //still runs shutdown hooks, vital for us to save stuff
        throw new RuntimeException("Unrecoverable condition", t); //We should absolutely kill the current thread though
    }

    /**
     * Reports a given error to {@link PrintStream stderr}, and exits
     * immediately. For good measure, this method will even throw a
     * {@link RuntimeException} to ensure the code flow is interrupted
     *
     * @param reason A short reason explaining why this failure occured
     * @param t The {2link Throwable} that caused this fatal crash
     * @throws RuntimeException A wrapper for the passed in {@link Throwable}
     */
    public static void reportAndExit(String reason, Throwable t) throws RuntimeException {
        t.printStackTrace();
        System.exit(1); //still runs shutdown hooks, vital for us to save stuff
        throw new RuntimeException(reason, t); //We should absolutely kill the current thread though
    }
}
