package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.math.BigDecimal;

public class ReturnCommand extends TextCommand {

    public ReturnCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("visitor-id")
                .list("id", 1);
    }

    @Override
    public String getName() {
        return "return";
    }

    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        //TODO: implement
        return ResponseFlag.SUCCESS;
    }
}
