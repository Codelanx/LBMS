package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.storage.StorageContainer;
import edu.rit.codelanx.data.storage.field.DataField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static edu.rit.codelanx.data.storage.field.FieldIndicies.FM_IMMUTABLE;
import static edu.rit.codelanx.data.storage.field.FieldIndicies.FM_KEY;

@StorageContainer("checkouts")
public class Checkout extends BasicState {

    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<Visitor> VISITOR;
        public static final DataField<Book> BOOK;
        public static final DataField<Instant> AT;
        public static final DataField<Boolean> RETURNED;
        private static final DataField<? super Object>[] VALUES;

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, VISITOR, BOOK, AT, RETURNED };
        }

        static {
            ID = DataField.makeIDField(Checkout.class);
            VISITOR = DataField.buildFromState(Visitor.class, "visitor", Visitor.Field.ID, FM_IMMUTABLE, FM_KEY);
            BOOK = DataField.buildFromState(Book.class, "book", Book.Field.ID, FM_IMMUTABLE, FM_KEY);
            AT = DataField.buildSimple(Instant.class, "at", FM_IMMUTABLE);
            RETURNED = DataField.buildSimple(Boolean.class, "returned");
            VALUES = Author.Field.values();
        }
    }

    Checkout(DataStorage storage, long id, StateBuilder<Checkout> builder) {
        super(storage, id, builder);
    }

    public Checkout(DataStorage storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    public Checkout(DataStorage storage, Map<String, Object> file) {
        super(storage, file);
    }

    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Checkout.Field.VALUES;
    }

    public Visitor getVisitor() {
        return Checkout.Field.VISITOR.get(this);
    }

    public Book getBook() {
        return Checkout.Field.BOOK.get(this);
    }

    public Instant getBorrowedAt() {
        return Checkout.Field.AT.get(this);
    }

    public boolean wasReturned() {
        return Field.RETURNED.get(this);
    }

    public void returnBook() {
        if (this.wasReturned()) {
            throw new IllegalStateException("Book already returned");
        }
        Duration d = Duration.between(this.getBorrowedAt(), Instant.now());
        //TODO: Determine if a fine should be applied
        if (false) {
            //TODO: And the amount (negative because we're taking from them)
            this.getVisitor().updateMoney(BigDecimal.valueOf(-1D), "late fee");
        }
        Field.RETURNED.set(this, true);
    }

    @Override
    public DataField<Long> getIDField() {
        return Checkout.Field.ID;
    }

    @Override
    public DataField<? super Object>[] getFields() {
        return Checkout.Field.values();
    }

    @Override
    public Type getType() {
        return StateType.CHECKOUT;
    }

    @Override
    public String toFormattedText() {
        String checkout= "Book Checkout: %s| Visitor ID: %d| at: %s| has been returned: %b";
        String formatted_ver= String.format(checkout, this.getBook().getTitle(), this.getVisitor().getID(),
        this.getBorrowedAt().toString(), this.wasReturned());

        return getFields().toString();
    }

    public static StateBuilder<Checkout> create() {
        return StateBuilder.of(Checkout::new, StateType.CHECKOUT, Field.ID, Field.VALUES);
    }

}
