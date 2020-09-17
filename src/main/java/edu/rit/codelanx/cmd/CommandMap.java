package edu.rit.codelanx.cmd;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.cmds.RegisterCommand;
import edu.rit.codelanx.util.Validate;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * A static mapping of our command classes
 */
public enum CommandMap {
    REGISTER(RegisterCommand::new);

    private static final CommandMap[] VALUES = CommandMap.values();
    private final Map<Server, Command<? extends Response>> serverCmd = new WeakHashMap<>();
    private final Function<Server, Command<? extends Response>> initializer;

    private CommandMap(Function<Server, Command<? extends Response>> initializer) {
        this.initializer = initializer;
    }

    public Command<? extends Response> toCommand(Server server) {
        return this.serverCmd.computeIfAbsent(server, this.initializer);
    }

    public static Optional<CommandMap> getMappingFor(String name) {
        Validate.nonNull(name, "Cannot map from null to a Command");
        String fname = name.toLowerCase();
        return Arrays.stream(VALUES)
                .filter(v -> {
                    if (v.serverCmd.isEmpty()) return false;
                    return v.serverCmd.values().iterator().next().getName().toLowerCase().equals(fname);
                }).findAny();
    }

    public static Command<? extends Response> getCommand(Server server, String cmdName) {
        Optional<? extends Command<? extends Response>> opt =
                CommandMap.getMappingFor(cmdName).map(c -> c.toCommand(server));
        return opt.isPresent() //overcoming the nested type bounding here with `opt` (it gets nasty)
                ? opt.get()
                : ResponseFlag.UNKNOWN.toDummyCommand();
    }

    public static void initialize(Server server) {
        Arrays.stream(VALUES).forEach(c -> c.toCommand(server));
    }
}
