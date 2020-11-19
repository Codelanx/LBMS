package edu.rit.codelanx.util;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Utility class to enforce assertions during program logic
 *
 * @author sja9291  Spencer Alderman
 */
public enum Validate {;

    /**
     * Asserts a given {@code exp} is true, and throws an exception with the
     * provided {@code type} and {@code reason} otherwise
     *
     * @param exp The expression to evaluate
     * @param reason The reason this expression must be {@code true}
     * @param type The class-type of the {@link RuntimeException} to throw
     *             if {@code false}
     * @see #isTrue(boolean, String) If using {@link IllegalArgumentException}
     */
    public static void isTrue(boolean exp, String reason, Class<? extends RuntimeException> type) {
        if (exp) return;
        RuntimeException out;
        try {
            Constructor<? extends RuntimeException> c = type.getConstructor(String.class);
            c.setAccessible(true);
            out = c.newInstance(reason);
        } catch (Throwable ex) {
            throw new UnsupportedOperationException(reason, ex);
        }
        throw out;
    }

    /**
     * Asserts a given {@code exp} is true, and throws an
     * {@link IllegalArgumentException} with the provided {@code reason}
     * otherwise
     *
     * @param exp The expression to evaluate
     * @param reason The reason this expression must be {@code true}
     * @see #isTrue(boolean, String, Class)
     */
    public static void isTrue(boolean exp, String reason) {
        Validate.isTrue(exp, reason, IllegalArgumentException.class);
    }

    /**
     * Asserts a given {@code value} is not {@code null}, and throws an
     * {@link NullPointerException} with the provided {@code reason}
     * otherwise
     *
     * @param value The value to check for nullity
     * @param reason An explanation of what reference was {@code null},
     *               if {@code value} is null
     * @see Objects#requireNonNull(Object, String)
     */
    public static void nonNull(Object value, String reason) {
        Objects.requireNonNull(value, reason);
    }
}
