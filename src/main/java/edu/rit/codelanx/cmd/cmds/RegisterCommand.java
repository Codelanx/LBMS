package edu.rit.codelanx.cmd.cmds;

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

        if (args.length < 1) {
            executor.sendMessage(this.getName() + ",missing-parameters," +
                    "visitorID");
            return ResponseFlag.SUCCESS;
        } else {

            // Creates a new Visitor id from the arguments
            Visitor newID = Visitor.create()
                    .setValue(Visitor.Field.FIRST, args[0])
                    .setValue(Visitor.Field.LAST, args[1])
                    .setValue(Visitor.Field.ADDRESS, args[3])
                    .setValue(Visitor.Field.PHONE, args[4])
                    .build(this.server.getDataStorage());
            newID.getID();
            executor.renderState(newID);

            // Checks to see if the new visitor information is valid
            if (!newID.isValid()) {
                executor.sendMessage("Invalid number of arguments!");
                return ResponseFlag.FAILURE;
            }

            return ResponseFlag.SUCCESS;
        }

    }
}
