package edu.rit.codelanx.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestValidate {

    @Test
    public void testIsTrue() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Validate.isTrue(false, "..."),
                "Did not throw correctly");
        Assertions.assertThrows(IllegalStateException.class,
                () -> Validate.isTrue(false, "should throw...", IllegalStateException.class),
                "Did not throw appropriate value");
        Assertions.assertDoesNotThrow(
                () -> Validate.isTrue(true, "shouldn't throw..."),
                "Invalid error thrown");
    }

    @Test
    public void testNonNull() {
        Assertions.assertThrows(NullPointerException.class,
                () -> Validate.nonNull(null, "whoops!"),
                "Did not throw error appropriately");
        Assertions.assertDoesNotThrow(
                () -> Validate.nonNull("hehe", "butts"),
                "Invalid error thrown");
    }
}
