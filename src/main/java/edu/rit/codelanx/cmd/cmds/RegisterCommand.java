package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.StateType;
import edu.rit.codelanx.data.state.types.Visit;
import edu.rit.codelanx.data.storage.field.DataField;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.state.types.Visitor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registers a new visitor so that they can access the library.
 * <p>
 * Request Format: register,first name,last name,address, phone-number
 * first name is the first name of the visitor.
 * last name is the last name of the visitor.
 * address is the address of the visitor.
 * phone-number is the phone number of the visitor.
 */
public class RegisterCommand extends TextCommand {

    /**
     * Constructor for the RegisterCommand class
     * @param server the server that the command is to be run on
     */
    public RegisterCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("first")
                .argument("last")
                .argument("address")
                .argument("phone-number");
    }

    /**
     * @link edu.rit.codelanx.cmd.Command#getName()
     */
    @Override
    public String getName() {
        return "register";
    }

    /**
     * Whenever this command is called, it will create a new visitor based on
     * the given data.
     *
     * @param executor  the client that is calling the command
     * @param args first name: the first name of the visitor.
     *                  last name: the last name of the visitor.
     *                  address: the address of the visitor.
     *                  phone-number: the phone number of the visitor.
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        //We use the builder pattern to create a new object in the data storage

        //We can assume all input is good - bounds are correct and no conversions to be done

        Instant registeredAt = Instant.now();

        // Creates a new Visitor id from the arguments
        Visitor newVisitor = Visitor.create()
                .setValue(Visitor.Field.FIRST, args[0])
                .setValue(Visitor.Field.LAST, args[1])
                .setValue(Visitor.Field.ADDRESS, args[2])
                .setValue(Visitor.Field.PHONE, args[3])
                .setValue(Visitor.Field.REGISTRATION_DATE, registeredAt)
                .setValue(Visitor.Field.MONEY, BigDecimal.ZERO)
                .build(this.server.getDataStorage());

        // Compares the inputted arguments to those already existing
        Visitor current = this.server.getDataStorage().query(Visitor.class)
                .isEqual(Visitor.Field.FIRST, args[0])
                .isEqual(Visitor.Field.LAST, args[1])
                .isEqual(Visitor.Field.ADDRESS, args[2])
                .isEqual(Visitor.Field.PHONE, args[3])
                .results().findAny().orElse(null);
        if (current != null) {
            //We already have a visitor with a matching first, last, address, AND phone number
            executor.sendMessage(this.buildResponse(this.getName(), "duplicate"));
            return ResponseFlag.SUCCESS;
        }


        executor.renderState(newVisitor);
        executor.sendMessage(this.buildResponse(this.getName(), newVisitor.getID(), DATE_FORMAT.format(registeredAt)));
        return ResponseFlag.SUCCESS;
    }
}
