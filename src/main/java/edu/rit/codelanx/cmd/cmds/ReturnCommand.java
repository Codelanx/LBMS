package edu.rit.codelanx.cmd.cmds;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.CommandExecutor;
import edu.rit.codelanx.cmd.ResponseFlag;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.cmd.text.TextParam;
import edu.rit.codelanx.data.state.types.Book;
import edu.rit.codelanx.data.state.types.Checkout;
import edu.rit.codelanx.data.state.types.Visitor;
import edu.rit.codelanx.network.io.TextMessage;
import edu.rit.codelanx.network.server.Server;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReturnCommand extends TextCommand {

    public ReturnCommand(Server<TextMessage> server) {
        super(server);
    }

    @Override
    protected TextParam.Builder buildParams() {
        return TextParam.create()
                .argument("visitor-id")
                .list("id", 1);
    }

    @Override
    public String getName() {
        return "return";
    }

    @Override
    public ResponseFlag onExecute(CommandExecutor executor, String... args) {
        Optional<Visitor> optVisitor = InputOutput.parseLong(args[0])
                .flatMap(id -> {
                    return this.server.getDataStorage().query(Visitor.class)
                            .isEqual(Visitor.Field.ID, id)
                            .results().findAny();
                });
        if (!optVisitor.isPresent()) {
            executor.sendMessage(this.buildResponse(this.getName(), "invalid-visitor-od"));
            return ResponseFlag.SUCCESS;
        }
        Visitor visitor = optVisitor.get();
        //parse the remainder of the arguments into book ids
        List<String> failed = new LinkedList<>();
        Set<Long> ids = new LinkedHashSet<>();
        for (int i = 0; i < args.length; i++) {
            Optional<Long> parsed = InputOutput.parseLong(args[i]);
            parsed.ifPresent(ids::add);
            if (!parsed.isPresent()) {
                failed.add(args[i]);
            }
        }
        //now, make sure the ids that we parsed are valid books
        Set<Book> books = this.server.getDataStorage().query(Book.class)
                .isAny(Book.Field.ID, ids)
                .results()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (books.size() != ids.size()) {
            //don't have time for fast disjoint sets, so...
            books.stream().map(Book::getID).forEach(ids::remove);
            //ids is now a set of invalid ids
            ids.stream().map(Object::toString).forEach(failed::add);
        }
        //Make sure the books are actually checked out
        List<Checkout> checkouts = this.server.getDataStorage().query(Checkout.class)
                .isEqual(Checkout.Field.VISITOR, visitor)
                .isAny(Checkout.Field.BOOK, books)
                .isEqual(Checkout.Field.RETURNED, false)
                .results()
                .collect(Collectors.toList());
        if (checkouts.size() != books.size()) {
            //again, invalid books because they weren't checks out
            checkouts.stream().map(Checkout::getBook).forEach(books::remove);
            //books is now the invalid copies
            books.stream().map(Book::getID)
                    .map(Object::toString)
                    .forEach(failed::add);
        }
        if (!failed.isEmpty()) {
            failed.add(0, this.getName());
            failed.add(1, "invalid-book-id");
            executor.sendMessage(this.buildResponse(failed));
            return ResponseFlag.SUCCESS;
        }
        //only successful checkouts found at this point
        List<String> fined = new LinkedList<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (Checkout checkout : checkouts) {
            BigDecimal fine = checkout.returnBook();
            if (fine != null) {
                fined.add(checkout.getBook().getID() + "");
                sum = sum.add(fine);
            }
        }
        if (fined.isEmpty()) {
            executor.sendMessage(this.buildResponse(this.getName(), "success"));
            return ResponseFlag.SUCCESS;
        }
        fined.add(0, this.getName());
        fined.add(1, "overdue");
        fined.add(2, String.format("$%.2d", sum.doubleValue()));
        executor.sendMessage(this.buildResponse(fined));
        return ResponseFlag.SUCCESS;
    }
}
