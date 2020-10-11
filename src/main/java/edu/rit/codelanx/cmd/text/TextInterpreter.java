package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.util.Validate;

import java.util.Map;
import java.util.WeakHashMap;

public class TextInterpreter implements Interpreter {

    private final Server<TextMessage> server;
    private final Map<CommandExecutor, StringBuilder> buffers = new WeakHashMap<>();

    public TextInterpreter(Server<TextMessage> server) {
        this.server = server;
        TextCommandMap.initialize(server); //Enables commands on this server
    }

    @Override
    public void receive(CommandExecutor executor, String data) {
        //TODO: Cleanup? Splitting up the method?
        char[] c = data.toCharArray();

        StringBuilder buffer = this.buffers.computeIfAbsent(executor, k -> new StringBuilder());
        for (int i = 0; i < c.length; i++) {
            char val = c[i];
            if (val == ';') {
                //we hit a terminator
                String full = buffer.toString();
                buffer.setLength(0); //clear the buffer
                this.execute(executor, full); //execute a received command
                continue; //continue checking for input
            }
            buffer.append(c[i]); //add to our future command buffer
        }
    }

    private String getDenialReason(TextCommand command, String... args) {
        //TODO: All pre-checks here
        //TODO: Check args against command#params for things like length, correctness, etc
        if (command.params == null) {
            return null; //TODO: command didn't implement params yet
        }
        if (command.params.length > 0 && args.length <= 0) { //if we need args, and there are none
            return command.buildResponse(command.getName(), "missing-params", command.getUsage()); //example of an error string
        }
        return null; //null if we're good!
    }

    private void execute(CommandExecutor executor, String command) {
        String[] args = command.split(",");
        String[] passedArgs = new String[args.length - 1];
        System.arraycopy(args, 1, passedArgs, 0, passedArgs.length);
        Command cmd = TextCommandMap.getCommand(this.server, args[0]);
        if (cmd instanceof TextCommand) {
            TextCommand tcmd = (TextCommand) cmd;
            String denial = this.getDenialReason(tcmd, passedArgs);
            if (denial != null) {
                executor.sendMessage(denial);
                return;
            }
        }
        ResponseFlag r = cmd.onExecute(executor, passedArgs);
        executor.sendMessage(r.getDescription()); //TODO: Remove in production
    }

}
