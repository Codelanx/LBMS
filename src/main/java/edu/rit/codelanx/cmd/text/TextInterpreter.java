package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.util.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//TODO: Double check if commands need to disable when the library is closed
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
        //Check args against command#params for things like length, correctness, etc
        if (command.params == null) {
            throw new UnsupportedOperationException("Command did not implement #buildParams correctly");
        }
        if (command.params.length > 0 && args.length <= 0) { //if we need args, and there are none
            return command.buildResponse(command.getName(), "missing-params", command.getUsage()); //example of an error string
        }
        if (command.params.length > args.length) {
            String suffix = IntStream.range(args.length, command.params.length)
                    .mapToObj(i -> command.params[i].toString())
                    .collect(Collectors.joining(TextCommand.TOKEN_DELIMITER));
            return command.buildResponse(command.getName(), "missing-params", suffix);
        }
        return null; //null if we're good!
    }

    private void execute(CommandExecutor executor, String command) {
        String[] args = this.splitInput(command);
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

    //splits the input appropriately
    private String[] splitInput(String input) {
        char[] c = input.toCharArray();
        List<String> back = new ArrayList<>();
        StringBuilder buff = new StringBuilder();
        chars:
        for (int i = 0; i < c.length; i++) {
            switch (c[i]) {
                case ',':
                    back.add(buff.toString());
                    buff.setLength(0);
                    break;
                case '{':
                    int end = input.indexOf('}', i);
                    String val = input.substring(i+1, end);
                    if (!val.isEmpty()) {
                        back.add(val);
                        i = end+1; //skip }
                        continue chars;
                    }
                    //fall through
                default:
                    buff.append(c[i]);
                    break;
            }
        }
        if (buff.length() > 0) {
            back.add(buff.toString());
        }
        return back.toArray(new String[0]);
    }

}
