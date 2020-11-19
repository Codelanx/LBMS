package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestDatetimeCommand {

    @Mock
    private Server<TextMessage> servMock;
    @Mock
    private CommandExecutor execMock;

    private DatetimeCommand dtSpy;
    private DatetimeCommand dt;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd,HH:mm:ss");
    private LocalDateTime now = LocalDateTime.now();
    private String formattedTime = dtf.format(now);

    @BeforeEach
    public void init(){
        MockitoAnnotations.initMocks(this);
        this.dt = new DatetimeCommand(this.servMock);
        this.dtSpy = Mockito.spy(dt);
        Mockito.doReturn(formattedTime).when(dtSpy).getClockTime();
    }


    @Test
    public void happyPathDays() {
        /*
        Test Explanation: Call datetime;
        Expectation: Should send datetime,YYYY/MM/DD,HH:MM:SS; and ResponseFlag.Success
        */
        Assertions.assertSame(ResponseFlag.SUCCESS, dtSpy.onExecute(execMock));
        Mockito.verify(dtSpy, Mockito.times(1)).getClockTime();
        Mockito.verify(execMock).sendMessage("datetime," + formattedTime + ";");
    }
}
