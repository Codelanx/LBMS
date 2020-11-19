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
    public void testNoInputAdvance() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: All inputs should be able to be handled, clock shouldn't change
         */
        //assertSame(ResponseFlag.SUCCESS, adv.onExecute(execMock, "",""));
        //Mockito.verify(execMock).sendMessage("advance,missing-parameters,number-of-days,number-of-hours;");
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
    public void tooMuchInputArrive() {
        /*
        Test Explanation: Testing sending too much input to the command
        Expectation: All inputs should be able to be handled, and visitor should arrive if their id is correct
         */
        //Mockito.when(visitorMock.isVisiting()).thenReturn(false);
        //Assertions.assertSame(ResponseFlag.SUCCESS, this.arrSpy.onExecute(this.execMock, INVALID_VISITOR_ID, "2"));
        //Mockito.verify(execMock).sendMessage("arrive,invalid-id;");
        //Assertions.assertSame(ResponseFlag.SUCCESS, this.arrSpy.onExecute(this.execMock, VALID_VISITOR_ID, "7"));
        //Mockito.verify(arrSpy, Mockito.times(1)).startVisit(any());
    }

    @Test
    public void testNoInputBorrow() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: All inputs should be able to be handled, no books will be checked out
         */
        //assertSame(ResponseFlag.SUCCESS, borSpy.onExecute(execMock, "",""));
        //Mockito.verify(borSpy, Mockito.never()).getBooks(anySet());
        //Mockito.verify(borSpy, Mockito.never()).getCheckedOut(any());
        //Mockito.verify(borSpy, Mockito.never()).checkout(any(), any());
        //Mockito.verify(execMock).sendMessage("borrow,missing-parameters,visitor-id,id;");
    }

    @Test
    public void testNoInputBorrowed() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: returns ResponseFlag.SUCCESS, but no borrowed books will
         be queried and printed out
         */
        //assertSame(ResponseFlag.SUCCESS, cmd_spy.onExecute(execMock, ""));
        //Mockito.verify(cmd_spy, Mockito.never()).getVisitor(any());
        //Mockito.verify(cmd_spy, Mockito.never()).getBorrowedBooks(any());
        //Mockito.verify(execMock).sendMessage("borrowed,missing-parameters,visitor-id;");
    }

    @Test
    public void tooMuchInputDateTime() {
        /*
        Test Explanation: Call datetime,anyArgument;
        Expectation: Should ignore other argument and send ResponseFlag.Success
        */
        //Assertions.assertSame(ResponseFlag.SUCCESS, dtSpy.onExecute(execMock, ""));
        //Mockito.verify(dtSpy, Mockito.times(1)).getClockTime();
        //Mockito.verify(execMock).sendMessage("datetime," + formattedTime + ";");
    }


    @Test
    public void testNoInputDepart() {
         /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: return SUCCESS response flag, but departing behaviour doesn't occur
         */
        //Assertions.assertEquals(ResponseFlag.SUCCESS, cmd.onExecute(this.execMock, " "));
        //Mockito.verify(cmd_spy, never()).getVisitor(any());
        //Mockito.verify(execMock).sendMessage("depart,missing-parameters,visitor-id");
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

    @Test
    public void testNoInputRegister() {
        /*
        Test Explanation: Testing sending no input/empty input to the command
        Expectation: All inputs should be able to be handled, no new visitor should be registered
         */
        //Mockito.doReturn(visitorMock).when(regSpy).findMatchingVisitor(any(), any(), any(), any());
        //Assertions.assertSame(ResponseFlag.SUCCESS, regSpy.onExecute(execMock, "", "", "", ""));
        //Assertions.assertSame(ResponseFlag.SUCCESS, regSpy.onExecute(execMock));
        //Assertions.assertSame(ResponseFlag.SUCCESS, regSpy.onExecute(execMock, FIRST_NAME, "", "", ""));
        //Assertions.assertSame(ResponseFlag.SUCCESS, regSpy.onExecute(execMock, FIRST_NAME, LAST_NAME, "", ""));
        //Assertions.assertSame(ResponseFlag.SUCCESS, regSpy.onExecute(execMock, FIRST_NAME, LAST_NAME, ADDRESS, ""));
        //Mockito.verify(regSpy, Mockito.never()).getRegistrationTime();
        //Mockito.verify(regSpy, Mockito.never()).createVisitor(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testNoInputReturn() {
        /*
        Test Explanation: Testing report when passed empty values
        Expectation: All inputs should be able to be handled, no book should
        be returned
         */
        //Assertions.assertSame(ResponseFlag.SUCCESS,
        //        retSpy.onExecute(this.execMock, "", ""));
        //Mockito.verify(execMock).sendMessage("return,missing-parameters," +
        //        "visitor,id;");
    }




}
