package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
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
 * @author cb4501 Connor Bonitati
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
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return "register";
    }

    /**
     * {@inheritDoc}
     * @param executor  {@inheritDoc}
     * @param args      {@inheritDoc}
     *                  args[0]: first name
     *                  args[1]: last name
     *                  args[2]: address
     *                  args[3]: phone number
     * @return {@inheritDoc}
     */
    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        return this.execute(executor, args[0], args[1], args[2], args[3]);
    }

    /**
     * Whenever this command is called, it will create a new visitor based on
     * the given data.
     *
     * @param executor  the client that is calling the command
     * @param fName: the first name of the visitor.
     * @param lName: the last name of the visitor.
     * @param address: the address of the visitor.
     * @param phoneNum: the phone number of the visitor.
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
    public ResponseFlag execute(CommandExecutor executor, String fName, String lName, String address, String phoneNum) {
        //We use the builder pattern to create a new object in the data storage

        //We can assume all input is good - bounds are correct and no conversions to be done

        // Compares the inputted arguments to those already existing
        Visitor current = findMatchingVisitor(fName, lName, address, phoneNum);
        if (current != null) {
            //We already have a visitor with a matching first, last, address, AND phone number
            executor.sendMessage(this.buildResponse(this.getName(), "duplicate"));
            return ResponseFlag.SUCCESS;
        }

        Instant registeredAt = getRegistrationTime();

        // Creates a new Visitor id from the arguments
        Visitor newVisitor = createVisitor(fName, lName, address, phoneNum, registeredAt);
        executor.sendMessage(this.buildResponse(this.getName(), newVisitor.getID(), DATE_FORMAT.format(registeredAt)));
        return ResponseFlag.SUCCESS;
    }

    /**
     * findMatchingVisitor is a helper method for {@link #onExecute} that gets a visitor that matches the fields input
     * @param fName the first name to filter by
     * @param lName the last name to filter by
     * @param address the address to filter by
     * @param phoneNum the phone number to filter by
     * @return the {@link Visitor} that matches, or else null
     */
    protected Visitor findMatchingVisitor(String fName, String lName, String address, String phoneNum){
        return this.server.getLibraryData().query(Visitor.class)
                .isEqual(Visitor.Field.FIRST, fName)
                .isEqual(Visitor.Field.LAST, lName)
                .isEqual(Visitor.Field.ADDRESS, address)
                .isEqual(Visitor.Field.PHONE, phoneNum)
                .results().findAny().orElse(null);
    }

    /**
     * getRegistrationTime is a helper method that gets the current time of the server
     * @return the current time of the server as an instant
     */
    protected Instant getRegistrationTime(){
        return this.server.getClock().getCurrentTime();
    }

    /**
     * createVisitor is a helper method for {@link #onExecute} that creates a visitor to be placed in the database
     * @param fName the first name of the visitor
     * @param lName the last name of the visitor
     * @param address the address of the visitor
     * @param phoneNum the phone number of the visitor
     * @param registeredAt the time the visitor was registered at
     * @return the {@link Visitor} that was created
     */
    protected Visitor createVisitor(String fName, String lName, String address, String phoneNum, Instant registeredAt){
        return Visitor.create()
                .setValue(Visitor.Field.FIRST, fName)
                .setValue(Visitor.Field.LAST, lName)
                .setValue(Visitor.Field.ADDRESS, address)
                .setValue(Visitor.Field.PHONE, phoneNum)
                .setValue(Visitor.Field.REGISTRATION_DATE, registeredAt)
                .setValue(Visitor.Field.MONEY, BigDecimal.ZERO)
                .build(this.server.getLibraryData());
    }
}
