package edu.rit.codelanx.cmd.text;

import com.codelanx.commons.util.ref.Box;
import edu.rit.codelanx.LBMS;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.Interpreter;
import edu.rit.codelanx.cmd.ResponseFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Parses and executes text content sent from a connected client
 *
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see Interpreter
 */
//TODO: Double check if commands need to disable when the library is closed
public class TextInterpreter implements Interpreter {

    //the server the commands run on
    private final Server<TextMessage> server;
    //buffers per connected executor
    private final Map<CommandExecutor, StringBuilder> buffers = new WeakHashMap<>();

    /**
     * Initializes the command map, making the commands available for general
     * use by any connected clients
     *
     * @param server The {@link Server} these commands are run on
     */
    public TextInterpreter(Server<TextMessage> server) {
        this.server = server;
        TextCommandMap.initialize(server); //Enables commands on this server
    }

    /**
     * {@inheritDoc}
     * @param executor {@inheritDoc}
     * @param data {@inheritDoc}
     */
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

    //Tries to find a reason to deny running the command
    private String getDenialReason(TextCommand command, Box<String[]> args) {
        //Check args against command#params for things like length, correctness, etc
        if (command.params == null) {
            throw new UnsupportedOperationException("Command did not implement #buildParams correctly");
        }
        if (command.params.length > 0 && args.value.length <= 0) { //if we need args, and there are none
            return command.buildResponse(command.getName(), "missing-params", command.getUsage()); //example of an error string
        }
        if (command.params.length > args.value.length) {
            String suffix = Arrays.stream(command.params, args.value.length, command.params.length)
                    .filter(TextParam::isRequired)
                    .map(TextParam::toString)
                    .collect(Collectors.joining(TextCommand.TOKEN_DELIMITER));
            if (suffix.isEmpty()) {
                //no required arguments were missed, we'll just hackily update the missing args
                String[] newArgs = new String[command.params.length];
                System.arraycopy(args.value, 0, newArgs, 0, args.value.length);
                Arrays.fill(newArgs, args.value.length, newArgs.length, "");
                args.value = newArgs; //REFACTOR: A little hacky, but gets the job done
            }
            return command.buildResponse(command.getName(), "missing-params", suffix);
        }
        return null; //null if we're good!
    }

    //finds the appropriate command and parses the input, then executes the command
    private void execute(CommandExecutor executor, String command) {
        String[] args = this.splitInput(command);
        String[] passedArgs = new String[args.length - 1];
        System.arraycopy(args, 1, passedArgs, 0, passedArgs.length);
        Command cmd = TextCommandMap.getCommand(this.server, args[0]);
        if (cmd instanceof TextCommand) {
            TextCommand tcmd = (TextCommand) cmd;
            Box<String[]> box = new Box<>();
            box.value = passedArgs;
            String denial = this.getDenialReason(tcmd, box);
            if (denial != null) {
                executor.sendMessage(denial);
                return;
            }
        }
        ResponseFlag r = cmd.onExecute(executor, passedArgs);
        if (LBMS.PREPRODUCTION_DEBUG) {
            executor.sendMessage(r.getDescription()); //TODO: Remove in production
        }
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
