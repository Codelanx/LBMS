package edu.rit.codelanx.cmd.cmds;

import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class TestRegisterCommand {

    private @Mock Server<TextMessage> serv;
    private @Mock CommandExecutor exec;

    @Test
    public void testNoInput() {
        RegisterCommand cmd = new RegisterCommand(this.serv);
        Assertions.assertSame(ResponseFlag.MISSING_ARGS, cmd.onExecute(this.exec));
        Mockito.verify(this.exec).sendMessage("register,missing-parameters,first name,last name,address,phone-number;");
    }

    @Test
    public void testBadInput() {
        RegisterCommand cmd = new RegisterCommand(this.serv);
        Assertions.assertSame(ResponseFlag.MISSING_ARGS,
                cmd.onExecute(this.exec, "bob", "mcthundergod"));
        Mockito.verify(this.exec).sendMessage("register,missing-parameters,address,phone-number;");
    }

    @Test
    public void testSuccess() {
        String last = "mcthundergod";
        long id = 1;
        RegisterCommand cmd = new RegisterCommand(this.serv);
        Assertions.assertSame(ResponseFlag.SUCCESS,
                cmd.onExecute(this.exec, "bob", last, "242 electric avenue", "555-555-HAND"));
        Mockito.verify(this.exec).sendMessage("register," + id + ";");
        Visitor visitor = this.serv.getDataStorage().query(Visitor.class)
                .isEqual(Visitor.Field.ID, id)
                .results().findAny().orElse(null);
        Assertions.assertNotNull(visitor);
        Assertions.assertEquals(last, visitor.getLastName());
        Assertions.assertEquals(id, visitor.getID());
    }

    @Test
    public void testDuplicate() {
        RegisterCommand cmd = new RegisterCommand(this.serv);
        Assertions.assertSame(ResponseFlag.SUCCESS,
                cmd.onExecute(this.exec, "1", "2", "3", "4"));
        Mockito.verify(this.exec).sendMessage("register,1;");
        Assertions.assertSame(ResponseFlag.SUCCESS,
                cmd.onExecute(this.exec, "1", "2", "3", "4"));
        Mockito.verify(this.exec).sendMessage("register,duplicate;");
    }
}
    /**
     * Whenever this command is called, it will create a new visitor based on
     * the given data.
     *
     * @param executor  the client that is calling the command
     * @param args      first name: the first name of the visitor.
     *                  last name: the last name of the visitor.
     *                  address: the address of the visitor.
     *                  phone-number: the phone number of the visitor.
     * @return a responseflag that says whether or not the command was
     * executed correctly
     */
/*    @Override
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
 */
