package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.data.cache.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

import static edu.rit.codelanx.data.cache.field.FieldIndicies.FM_IMMUTABLE;
import static edu.rit.codelanx.data.cache.field.FieldIndicies.FM_KEY;

/**
 * A {@link BasicState} represents a complete Visit
 * @author sja9291  Spencer Alderman
 * @author ahd6901  Amy Ha Do
 * @see BasicState
 */
@StorageContainer("visits")
public class Visit extends BasicState {

    /**
     * String representation of a visit
     *
     * @return string
     */
    @Override
    public String toFormattedText() {
        Instant start = this.getStart();
        Instant end = this.getEnd();
        String visit;
        visit = "Visit ID: %d| Visitor ID %d| Visit Duration: %s";
        String formatted_visit = String.format(visit, this.getID(), this.getVisitor().getID(),
                this.getDuration().toString());
        return formatted_visit;
    }


    /**
     * gets the visit duration
     * @return Duration
     */
    public Duration getDuration() {
        return Duration.between(this.getStart(), this.getEnd());
    }

    /**
     * format time (type instant) into a string
     *
     * @param time- to be formatted
     * @return string representation of time
     */
    public String format_time(Instant time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.US).withZone(ZoneId.systemDefault());
        return formatter.format(time);
    }


    public static class Field {
        public static final DataField<Long> ID;
        public static final DataField<Visitor> VISITOR;
        public static final DataField<Instant> START;
        public static final DataField<Instant> END;
        private static final DataField<? super Object>[] VALUES;

        /**
         * gets visit data
         * @return visit, visitor, start time, end time.
         */
        public static DataField<? super Object>[] values() {
            return new DataField[]{ID, VISITOR, START, END};
        }

        static {
            ID = DataField.makeIDField(Checkout.class);
            VISITOR = DataField.buildFromState(Visitor.class, "visitor", Visitor.Field.ID, FM_IMMUTABLE, FM_KEY);
            START = DataField.buildSimple(Instant.class, "start", FM_IMMUTABLE);
            END = DataField.buildSimple(Instant.class, "end", FM_IMMUTABLE);
            VALUES = Field.values();
        }
    }
    /** @see BasicState#BasicState(DataSource, long, StateBuilder)  */
    Visit(DataSource storage, long id, StateBuilder<Visit> builder) {
        super(storage, id, builder);
    }

    /** @see BasicState#BasicState(DataSource, ResultSet) */
    public Visit(DataSource storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    /** @see BasicState#BasicState(DataSource, Map) */
    public Visit(DataSource storage, Map<String, Object> file) {
        super(storage, file);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected DataField<? super Object>[] getFieldsUnsafe() {
        return Field.VALUES;
    }

    /**
     * gets the visitor of this visit
     * @return {@link Visitor}
     */
    public Visitor getVisitor() {
        return Field.VISITOR.get(this);
    }

    /**
     * gets the time the visit begins
     * @return begin time type {@link Instant}
     */
    public Instant getStart() {
        return Field.START.get(this);
    }

    /**
     * gets the time the visit ends
     * @return end time {@link Instant}
     */
    public Instant getEnd() {
        return Field.END.get(this);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataField<Long> getIDField() {
        return Field.ID;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataField<? super Object>[] getFields() {
        return Field.values();
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Type getType() {
        return StateType.VISIT;
    }

    /**
     * creates a visit
     * @return {@link StateBuilder} of Visit
     */
    public static StateBuilder<Visit> create() {
        return StateBuilder.of(Visit::new, StateType.VISIT, Field.ID, Field.VALUES);
    }
}
