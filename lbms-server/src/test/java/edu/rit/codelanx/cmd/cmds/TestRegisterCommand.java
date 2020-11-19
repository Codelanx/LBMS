package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.mockito.Matchers.any;

public class TestRegisterCommand {

    private final String FIRST_NAME = "Joe";
    private final String LAST_NAME = "Mama";
    private final String ADDRESS = "1428 Elm Street, Springwood, Ohio";
    private final String PHONE = "951-572-2602";

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;
    @Mock
    private Visitor visitorMock;

    private RegisterCommand reg;
    private RegisterCommand regSpy;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.reg = new RegisterCommand(this.servMock);
        this.regSpy = Mockito.spy(reg);
    }


    @Test
    public void testDuplicate() {
        /*
        Test Explanation: Testing registering a person that has already been added
        Expectation: All inputs should be able to be handled, no new visitor should be registered
         */
        Mockito.doReturn(visitorMock).when(regSpy).findMatchingVisitor(any(), any(), any(), any());
        Assertions.assertSame(ResponseFlag.SUCCESS, regSpy.onExecute(execMock, FIRST_NAME, LAST_NAME, ADDRESS, PHONE));
        Mockito.verify(regSpy, Mockito.never()).getRegistrationTime();
        Mockito.verify(regSpy, Mockito.never()).createVisitor(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void happyPath() {
        /*
        Test Explanation: Testing the command being run correctly
        Expectation: All inputs should be able to be handled, new visitor should be registered
         */
        Mockito.doReturn(null).when(regSpy).findMatchingVisitor(any(), any(), any(), any());
        Mockito.doReturn(Instant.now()).when(regSpy).getRegistrationTime();
        Mockito.doReturn(visitorMock).when(regSpy).createVisitor(any(), any(), any(), any(), any(), any());
        Assertions.assertSame(ResponseFlag.SUCCESS, regSpy.onExecute(execMock, FIRST_NAME, LAST_NAME, ADDRESS, PHONE));
        Mockito.verify(regSpy, Mockito.times(1)).getRegistrationTime();
        Mockito.verify(regSpy, Mockito.times(1)).createVisitor(any(), any(), any(), any(), any(), any());
    }
}

