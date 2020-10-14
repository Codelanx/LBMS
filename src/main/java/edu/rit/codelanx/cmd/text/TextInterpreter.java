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
import java.util.LinkedList;
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

    /**
     * the wildcards as specified in the LBMS command doc
     */
    public static final String INPUT_WILDCARD = "*";
    /**
     * What we identify as a wildcard in the arguments
     */
    public static final String OUTPUT_WILDCARD = "";
    //the server the commands run on
    private final Server<TextMessage> server;
    //buffers per connected executor
    private final Map<CommandExecutor, StringBuilder> buffers =
            new WeakHashMap<>();

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
     *
     * @param executor {@inheritDoc}
     * @param data     {@inheritDoc}
     */
    @Override
    public void receive(CommandExecutor executor, String data) {
        char[] c = data.toCharArray();

        StringBuilder buffer = this.buffers.computeIfAbsent(executor,
                k -> new StringBuilder());
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

    /**
     * Provides a method for splitting a given {@link String} into an array
     * of arguments. Lists of values enclosed in braces e.g. {@code {1,2,3}}
     * will simply return the inner result as a single argument, e.g.
     * {@code "1,2,3"}
     *
     * @param input The {@link String} to parse
     * @return The parsed {@link String[]} arguments
     */
    public static String[] splitInput(String input) {
        char[] c = input.toCharArray();
        List<String> back = new ArrayList<>();
        StringBuilder buff = new StringBuilder();
        chars:
        for (int i = 0; i < c.length; i++) {
            switch (c[i]) {
                case ',':
                    String ex = buff.toString();
                    //map * here too
                    back.add(ex.equals(INPUT_WILDCARD) ? OUTPUT_WILDCARD : ex);
                    buff.setLength(0);
                    break;
                case '{':
                    int end = input.indexOf('}', i);
                    String val = input.substring(i + 1, end);
                    if (!val.isEmpty()) {
                        back.add(val);
                        i = end + 1; //skip }
                        continue chars;
                    }
                    //fall through
                default:
                    buff.append(c[i]);
                    break;
            }
        }
        if (buff.length() > 0) {
            String ex = buff.toString();
            back.add(ex.equals(INPUT_WILDCARD) ? OUTPUT_WILDCARD : ex);
        }
        return back.toArray(new String[0]);
    }

    //finds the appropriate command and parses the input, then executes the
    // command
    private void execute(CommandExecutor executor, String command) {
        String[] args = TextInterpreter.splitInput(command);
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
            passedArgs = this.preprocessArguments(tcmd, passedArgs);
        }
        ResponseFlag r = cmd.onExecute(executor, passedArgs);
        if (LBMS.PREPRODUCTION_DEBUG) {
            executor.sendMessage(r.getDescription()); //TODO: Remove in
            // production
        }
    }

    //Tries to find a reason to deny running the command
    private String getDenialReason(TextCommand command, String... args) {
        //Check args against command#params for things like length,
        // correctness, etc
        if (command.params == null) {
            throw new UnsupportedOperationException("Command did not implement #buildParams correctly");
        }
        if (command.params.length > 0 && args.length <= 0) { //if we need
            // args, and there are none
            return command.buildResponse(command.getName(), "missing-params",
                    command.getUsage()); //example of an error string
        }
        if (command.params.length > args.length) {
            String suffix = Arrays.stream(command.params, args.length,
                    command.params.length)
                    .filter(TextParam::isRequired)
                    .map(TextParam::toString)
                    .collect(Collectors.joining(TextCommand.TOKEN_DELIMITER));
            if (!suffix.isEmpty()) {
                return command.buildResponse(command.getName(),
                        "missing-params", suffix);
            }
        }
        List<String> badWilds = IntStream.range(0, args.length - 1)
                .filter(i -> command.params[i].isRequired())
                .filter(i -> args[i].isEmpty() || args[i].equals(INPUT_WILDCARD))
                .mapToObj(i -> command.params[i].getLabel())
                .collect(Collectors.toCollection(LinkedList::new));
        if (badWilds.size() > 0) {
            badWilds.add(0, command.getName());
            badWilds.add(1, "missing-params");
            return command.buildResponse(badWilds);
        }
        return null; //null if we're good!
    }

    private String[] preprocessArguments(TextCommand command, String... args) {
        //TODO: Quick Patch for fixing commands with no args being passed args
        if (command.params.length == 0){
            return new String[]{};
        }
        IntStream.range(0, args.length)
                .filter(i -> args[i] == null)
                .forEach(i -> args[i] = OUTPUT_WILDCARD);
        String[] sized;
        if (command.params.length == args.length) {
            sized = args;
        } else {
            //Map mismatched array sizes
            int copyLength = command.params.length; //how much we can copy from args
            if (args.length > command.params.length) {
                //TODO: added the -1 because we were getting oob errors
                if (command.params[command.params.length - 1].isList()) {
                    //Ignore excess arguments if not a list
                    copyLength = args.length;
                }
            } else {
                //Check to see if any of the remaining arguments are required
                if (Arrays.stream(command.params, args.length, command.params.length)
                        .anyMatch(TextParam::isRequired)) {
                    throw new IllegalArgumentException("Missed required arguments");
                }
            }
            String[] newArgs = new String[copyLength];
            //copy the input available
            System.arraycopy(args, 0, newArgs, 0, args.length);
            if (args.length < newArgs.length) {
                //buffer optional arguments with wildcards
                Arrays.fill(newArgs, args.length, newArgs.length,
                        OUTPUT_WILDCARD);
            }
            sized = newArgs;
        }
        //Map star inputs to empty strings to indicate "wildcards"
        IntStream.range(0, sized.length)
                .filter(i -> !command.params[i].isRequired())
                .filter(i -> sized[i].equals(INPUT_WILDCARD))
                .forEach(i -> sized[i] = OUTPUT_WILDCARD);
        return sized;
    }

}
