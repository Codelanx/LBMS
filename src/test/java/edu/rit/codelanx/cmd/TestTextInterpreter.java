package edu.rit.codelanx.cmd;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

public class TestTextInterpreter {

    @Test
    public void attempt() {
        IntStream.range(0, 100)
                .filter(i -> i % 10 == 0)
                .map(i -> i + 5)
                .sum();

        IntStream.range(0, 100).boxed()
                .filter(i -> i % 10 == 0)
                .map(i -> i + 5)
                .reduce(0, Integer::sum); //(0 + 0) + 1) + 2) + 3) + 4) + 5)

        int identity = 0;
        int sum = identity;
        for (int i = 0; i < 100; i++) {
            if (!(i % 10 == 0)) {
                continue; //skip, filter false
            }
            int newI = i + 5;
            sum = Integer.sum(sum, newI);
        }
    }
}
