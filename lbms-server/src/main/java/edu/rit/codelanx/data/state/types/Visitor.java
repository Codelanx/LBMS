package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.field.DataField;
import edu.rit.codelanx.data.field.FieldIndicies;
import edu.rit.codelanx.data.storage.StateBuilder;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.data.DataSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BasicState} presents a visitor
 *
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see BasicState
 */
@StorageContainer("visitors")
public class Visitor extends BasicState {

    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<String> FIRST;
        public static final DataField<String> LAST;
        public static final DataField<String> ADDRESS;
        public static final DataField<String> PHONE;
        public static final DataField<Instant> REGISTRATION_DATE;
        public static final DataField<BigDecimal> MONEY;


        private static final DataField<? super Object>[] VALUES;

        /**
         * gets the visitor data
         *
         * @return id, name, address, phone, balance
         */
        public static DataField<? super Object>[] values() {
            return new DataField[]{ID, FIRST, LAST, ADDRESS, PHONE,
                    REGISTRATION_DATE, MONEY};
        }

        static {
            ID = DataField.makeIDField(Book.class);
            FIRST = DataField.buildSimple(String.class, "first", FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY, FieldIndicies.FM_COMPOSITE);
            LAST = DataField.buildSimple(String.class, "last", FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY, FieldIndicies.FM_COMPOSITE);
            ADDRESS = DataField.buildSimple(String.class, "address", FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_COMPOSITE);
            PHONE = DataField.buildSimple(String.class, "phone", FieldIndicies.FM_IMMUTABLE, FieldIndicies.FM_KEY, FieldIndicies.FM_COMPOSITE);
            REGISTRATION_DATE = DataField.buildSimple(Instant.class, "registration_date", FieldIndicies.FM_IMMUTABLE);
            MONEY = DataField.buildSimple(BigDecimal.class, "money");
            VALUES = Field.values();
        }
    }

    //TODO: How do my little side branches keep making it into main?
    // Aghhhhh
    public static class Key {
        //unique keys
        //composite keys
        //mapping keys
    }

    //keeps track of an ongoing visit. If it ends, a `Visit` is added to the data
    //Consequently if the library explodes in an act of terrorism, we won't have a log of that visit
    //So maybe rethink this part of our design. Because terrorists.
    private final AtomicReference<Instant> visitStart = new AtomicReference<>(null);

    //Behavioral methods here

    /**
     * gets the visitor's first name
     *
     * @return string first name
     */
    public String getFirstName() {
        return Field.FIRST.get(this);
    }

    /**
     * gets the visitor's last name
     *
     * @return string last name
     */
    public String getLastName() {
        return Field.LAST.get(this);
    }

    /**
     * gets the visitor's address
     *
     * @return string address
     */
    public String getAddress() {
        return Field.ADDRESS.get(this);
    }

    /**
     * gets the visitor phone number
     *
     * @return string phone number
     */
    public String getPhone() {
        return Field.PHONE.get(this);
    }

    /**
     * gets the amount of money the visitor currently owed
     *
     * @return money decimal
     */
    public BigDecimal getMoney() {
        return Field.MONEY.get(this);
    }

    /**
     * starts the visit if it's within the library is currently open
     *
     * @param library {@link Library} to be check for open status
     * @return true if successfully start a visit, otherwise, false
     */
    public boolean startVisit(Library library) {
        if (!library.isOpen()){
            return false;
        }
        this.visitStart.set(library.getClock().getCurrentTime());
        return true;
    }

    /**
     * checks if the visitor is in a visit or not
     *
     * @return true if currently in the visit, otherwise, false
     */
    public boolean isVisiting() {
        return this.getVisitStart() != null;
    }

    public Instant getVisitStart() {
        return this.visitStart.get();
    }

    /**
     * ends the visit
     *
     * @param endTime of the visit
     * @return true if successfully ends a visit. Otherwise, false.
     */
    public Visit endVisit(Instant endTime) {
        Instant start = this.visitStart.getAndUpdate(k -> null);
        if (start == null) return null;
        return Visit.create()
                .setValue(Visit.Field.VISITOR, this)
                .setValue(Visit.Field.START, start)
                .setValue(Visit.Field.END, endTime)
                .build(this.getLoader());
    }

    /**
     * gets the visitor id
     *
     * @return id of long type
     */
    @Override
    public DataField<Long> getIDField() {
        return Field.ID;
    }

    /**
     * gets visitor info
     *
     * @return DataField of ID, FIRST, LAST, ADDRESS, PHONE, MONEY
     */
    @Override
    public DataField<? super Object>[] getFields() {
        return Field.values();
    }

    /**
     * gets he current state
     *
     * @return visitor state
     */
    @Override
    public Type getType() {
        return StateType.VISITOR;
    }

    /**
     * updates the visitor balance
     *
     * @param amount of money to set to
     * @param reason for the new update
     * @return visitor's new balance
     */
    public BigDecimal updateMoney(BigDecimal amount, String reason) {
        Transaction.create()
                .setValue(Transaction.Field.VISITOR, this)
                .setValue(Transaction.Field.MONEY, amount)
                .setValue(Transaction.Field.REASON, reason)
                .build(this.getLoader());
        return Field.MONEY.mutate(this, amount::add);
    }

    /**
     * Provides a new {@link StateBuilder} to allow creating these State
     * objects on a later-provided {@link DataSource}
     *
     * @return The {@link StateBuilder} to build off of
     */
    //Creational handling below
    public static StateBuilder<Visitor> create() {
        return StateBuilder.of(Visitor::new, StateType.VISITOR, Field.ID,
                Field.VALUES);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
    }


    //Storage serialization handling below


    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param id {@inheritDoc}
     * @param builder {@inheritDoc}
     * @see BasicState#BasicState(DataSource, long, StateBuilder)
     */
    Visitor(DataSource storage, long id, StateBuilder<Visitor> builder) {
        super(storage, id, builder);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param sql {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @see BasicState#BasicState(DataSource, ResultSet)
     */
    public Visitor(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /**
     * {@inheritDoc}
     * @param storage {@inheritDoc}
     * @param file {@inheritDoc}
     * @see BasicState#BasicState(DataSource, Map)
     */
    public Visitor(DataSource storage, Map<String, Object> file) {
        super(storage, file);
    }

}
