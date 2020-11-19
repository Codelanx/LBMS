package edu.rit.codelanx.cmd;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

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



    @Test
    public void testNoInputArrive() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: All inputs should be able to be handled
         */
        //Assertions.assertSame(ResponseFlag.SUCCESS, arrSpy.onExecute(execMock, ""));
        //Mockito.verify(execMock).sendMessage("arrive,missing-parameters,visitor-id;");
    }



    @Test
    public void TestNoInputPay() {
           /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: return SUCCESS response flag, but not actually paying
         */
        //Assertions.assertEquals(ResponseFlag.SUCCESS, cmd_spy.onExecute(this.execMock, "", ""));
        //verify(cmd_spy, never()).getVisitor(any());
        //verify(cmd_spy, never()).performPayTransaction(any(), any());
    }
}
