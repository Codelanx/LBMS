package edu.rit.codelanx.cmd.text;

import edu.rit.codelanx.cmd.Command;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.cmds.*;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.util.Validate;
import edu.rit.codelanx.network.server.Server;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * A static mapping of our command classes
 */
public enum TextCommandMap {
    ADVANCE(AdvanceCommand::new),
    ARRIVE(ArriveCommand::new),
    BORROW(BorrowCommand::new),
    BORROWED(BorrowedCommand::new),
    BUY(BuyCommand::new),
    DATETIME(DatetimeCommand::new),
    DEPART(DepartCommand::new),
    INFO(InfoCommand::new),
    PAY(PayCommand::new),
    REGISTER(RegisterCommand::new),
    REPORT(ReportCommand::new),
    SEARCH(SearchCommand::new),
    RETURN(ReturnCommand::new),
    ;

    private static final TextCommandMap[] VALUES = TextCommandMap.values();
    private final Map<Server<TextMessage>, Command> serverCmd = new WeakHashMap<>();
    private final Function<Server<TextMessage>, Command> initializer;

    private TextCommandMap(Function<Server<TextMessage>, Command> initializer) {
        this.initializer = initializer;
    }

    public Command toCommand(Server<TextMessage> server) {
        return this.serverCmd.computeIfAbsent(server, this.initializer);
    }

    public static Optional<TextCommandMap> getMappingFor(String name) {
        Validate.nonNull(name, "Cannot map from null to a Command");
        String fname = name.toLowerCase();
        return Arrays.stream(VALUES)
                .filter(v -> {
                    if (v.serverCmd.isEmpty()) return false;
                    return v.serverCmd.values().iterator().next().getName().toLowerCase().equals(fname);
                }).findAny();
    }

    public static Command getCommand(Server<TextMessage> server, String cmdName) {
        Optional<? extends Command> opt =
                TextCommandMap.getMappingFor(cmdName).map(c -> c.toCommand(server));
        return opt.isPresent() //overcoming the nested type bounding here with `opt` (it gets nasty)
                ? opt.get()
                : ResponseFlag.UNKNOWN.toDummyCommand();
    }

    public static void initialize(Server<TextMessage> server) {
        Arrays.stream(VALUES).forEach(c -> c.toCommand(server));
    }
}
