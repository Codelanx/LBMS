package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
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
            return (long)-1;
        }
    }

    //Searching for a visitor in the database
    public static Visitor findVisitor(Server<TextMessage> server,
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
    public static UtilsFlag checkDuplicateVisitor(Server<TextMessage> server,
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
    public static UtilsFlag alreadyVisiting(Server<TextMessage> server, long visitorID) {
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

    public static List<Object> checkTimeAdvance(String[] args){
        String daysString = args[0];
        UtilsFlag resultFlag;
        int daysInt;
        int hoursInt;
        List<Object> result = new ArrayList<>();
        //Seeing if they gave us days and hours or just hours
        if (args.length == 2) {
            String hoursString = args[1];
            try{
                hoursInt = Integer.parseInt(hoursString);
                if (hoursInt > 23 || hoursInt < 0){
                    result.add(resultFlag = UtilsFlag.INVALIDHOURS);
                    result.add(daysInt = 0);
                    result.add(hoursInt = 0);
                    return result;
                }
            } catch (NumberFormatException e){
                result.add(resultFlag = UtilsFlag.ERROR);
                result.add(daysInt = 0);
                result.add(hoursInt = 0);
                return result;
            }
        } else {
            hoursInt = 0;
        }

        try{
            daysInt = Integer.parseInt(daysString);
            if (daysInt > 7 || daysInt < 0){
                result.add(resultFlag = UtilsFlag.INVALIDDAYS);
                result.add(daysInt = 0);
                result.add(hoursInt = 0);
                return result;
            }
        } catch (NumberFormatException e){
            result.add(resultFlag = UtilsFlag.ERROR);
            result.add(daysInt = 0);
            result.add(hoursInt = 0);
            return result;
        }

        result.add(resultFlag = UtilsFlag.CORRECT);
        result.add(daysInt);
        result.add(hoursInt);
        return result;
    }

}
