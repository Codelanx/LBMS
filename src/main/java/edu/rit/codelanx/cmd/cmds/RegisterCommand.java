package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.data.types.Visitor;

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
     * @param arguments first name: the first name of the visitor.
     *                  last name: the last name of the visitor.
     *                  address: the address of the visitor.
     *                  phone-number: the phone number of the visitor.
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... arguments) {
        //We use the builder pattern to create a new object in the data storage
        Visitor.Builder builder;
        //TODO: Fill out visitor's data from real arguments
        builder = Visitor.create(this.server.getDataStorage())
                .firstName("Bob")
                .lastName("RadicalAndDangerous")
                .address("242 Electric Avenue")
                .phone("555-555-BRAD")
                .money(BigDecimal.valueOf(2.00));
        //check if it's valid (our example always should be)
        if (!builder.isValid()) {
            executor.sendMessage("Invalid number of arguments!");
            return ResponseFlag.FAILURE;
        }
        Visitor registered = builder.build();
        //TODO: Maybe do something with our new user? It's already managed in DataStorage though
        return ResponseFlag.SUCCESS;
    }
}
