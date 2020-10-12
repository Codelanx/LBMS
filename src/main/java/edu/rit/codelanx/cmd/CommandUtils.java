package edu.rit.codelanx.cmd;

import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Long.parseLong;

/**
 * @author maa1675  Mark Anderson
 * @author sja9291  Spencer Alderman    small fixes
 */
@Deprecated
public enum CommandUtils {;

    //Checking that a visitorID is valid
    @Deprecated
    public static Long checkVisitorID(String visitorID) {
        try {
            return parseLong(visitorID);
        } catch (NumberFormatException n) {
            return (long)-1;
        }
    }

    //Searching for a visitor in the database
    @Deprecated
    public static Visitor findVisitor(Server<TextMessage> server,
                                      long visitorID) {
        if (true) {
            //This method doesn't perform a query, it used an old broken method
            throw new UnsupportedOperationException("This doesn't do what you think it does!");
        }
        Optional<? extends Visitor> visitorSearch = Optional.empty();/*server.getDataStorage()
                .ofLoaded(Visitor.class) //TODO: Fix
                .filter(v -> v.getID() == visitorID)
                .findAny();*/
        return visitorSearch.orElse(null);
    }

    //Checking the amount of arguments for a command
    @Deprecated
    public static UtilsFlag numArgs(String[] args, int expected) {
        if (args.length < expected) {
            return UtilsFlag.MISSINGPARAMS;
        } else {
            return UtilsFlag.CORRECT;
        }
    }

    //Checking that the visitor isn't in the database already
    @Deprecated
    public static UtilsFlag checkDuplicateVisitor(Server<TextMessage> server,
                                                  long visitorID) {
        if (true) {
            //Note: we never allow adding a duplicate visitor in the first place, it would create an error
            //Instead, TODO: need to work out a way to detect this without an error (it's in the works under Visitor/IndexAllUnique)
            throw new UnsupportedOperationException("This method doesn't do what you think it does!");
        }
        long visitorCount = 2;/*server.getDataStorage() //this was broken anyhow
                .ofLoaded(Visitor.class)
                .filter(v -> v.getID() == visitorID)
                .count();*/
        if (visitorCount > 1) {
            return UtilsFlag.DUPLICATEVISITOR;
        } else {
            return UtilsFlag.CORRECT;
        }
    }

    @Deprecated
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
