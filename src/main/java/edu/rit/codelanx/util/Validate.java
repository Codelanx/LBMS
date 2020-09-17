package edu.rit.codelanx.util;

import java.lang.reflect.Constructor;
import java.util.Objects;

public enum Validate {
    ;

    public static void isTrue(boolean exp, String failureReason, Class<? extends Throwable> type) {
        if (exp) return;
        try {
            Constructor<? extends Throwable> c = type.getConstructor(String.class);
            c.setAccessible(true);
            throw (Throwable) c.newInstance(failureReason);
        } catch (Throwable ex) {
            throw new UnsupportedOperationException(failureReason, ex);
        }
    }

    public static void isTrue(boolean exp, String failureReason) {
        Validate.isTrue(exp, failureReason, IllegalArgumentException.class);
    }

    public static void nonNull(Object value, String failureReason) {
        Objects.requireNonNull(value, failureReason);
    }
}
