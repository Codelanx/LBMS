package edu.rit.codelanx.cmd;

import edu.rit.codelanx.Server;
import edu.rit.codelanx.cmd.Response;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.UtilsFlag;
import edu.rit.codelanx.data.types.Visit;
import edu.rit.codelanx.data.types.Visitor;
import jdk.internal.net.http.common.Pair;

import java.util.Optional;

import static java.lang.Long.parseLong;

public final class CommandUtils {

    // Private constructor to prevent instantiation
    private CommandUtils() {
        throw new UnsupportedOperationException();
    }

    //Checking that a visitorID is valid
    public static Long checkVisitorID(String visitorID) {
        try {
            return parseLong(visitorID);
        } catch (NumberFormatException n) {
            return null;
        }
    }

    //Searching for a visitor in the database
    public static Visitor findVisitor(Server server,
                                                       long visitorID) {
        Optional<? extends Visitor> visitorSearch = server.getDataStorage()
                .ofLoaded(Visitor.class)
                .filter(v -> v.getID() == visitorID)
                .findAny();
        return visitorSearch.orElse(null);
    }

    //Checking the amount of arguments for a command
    public static UtilsFlag numArgs(String[] args, int expected) {
        if (args.length < expected) {
            return UtilsFlag.MISSINGPARAMS;
        } else {
            return UtilsFlag.CORRECT;
        }
    }

    //Checking that the visitor isn't in the database already
    public static UtilsFlag checkDuplicateVisitor(Server server,
                                                  long visitorID) {
        long visitorCount = server.getDataStorage()
                .ofLoaded(Visitor.class)
                .filter(v -> v.getID() == visitorID)
                .count();
        if (visitorCount > 1) {
            return UtilsFlag.DUPLICATEVISITOR;
        } else {
            return UtilsFlag.CORRECT;
        }
    }

    //Checks that a visitor is not already visiting the library
    public static UtilsFlag alreadyVisiting(Server server, long visitorID) {
        Optional<? extends Visitor> visitorSearch = server.getDataStorage()
                .ofLoaded(Visitor.class)
                .filter(v -> v.getID() == visitorID)
                .findAny();
        if (visitorSearch.isPresent()) {
            Visitor visitor = visitorSearch.get();
            if (visitor.isVisiting()) {
                return UtilsFlag.ALREADYVISITING;
            } else {
                return UtilsFlag.CORRECT;
            }
        }
        return UtilsFlag.CORRECT;
    }


}
