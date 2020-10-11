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

    private void execute(CommandExecutor executor, String command) {
        String[] args = command.split(",");
        String[] passedArgs = new String[args.length - 1];
        System.arraycopy(args, 1, passedArgs, 0, passedArgs.length);
        Command cmd = TextCommandMap.getCommand(this.server, args[0]);
        ResponseFlag r = cmd.onExecute(executor, passedArgs);
        if (r == ResponseFlag.MISSING_ARGS && cmd instanceof TextCommand) {
            TextCommand tcmd = (TextCommand) cmd;
            String[] data = tcmd.missingArgs;
            Validate.nonNull(data, "Did not set missing argument parameters!");
            String out = String.join(",", data);
            executor.sendMessage(out);
        } else {
            executor.sendMessage(r.getDescription()); //TODO: Remove in production
        }
        //TODO: Handle r
    }

}
