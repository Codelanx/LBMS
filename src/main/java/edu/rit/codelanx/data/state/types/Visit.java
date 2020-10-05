package edu.rit.codelanx.data.state.types;

import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.BasicState;
import edu.rit.codelanx.data.loader.StateBuilder;
import edu.rit.codelanx.data.storage.StorageContainer;
import edu.rit.codelanx.data.storage.field.DataField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

import static edu.rit.codelanx.data.storage.field.FieldModifier.FM_IMMUTABLE;
import static edu.rit.codelanx.data.storage.field.FieldModifier.FM_KEY;
import static edu.rit.codelanx.data.storage.field.FieldModifier.FM_UNIQUE;

@StorageContainer("visits")
public class Visit extends BasicState {

    /**
     * String representation of a visit (arrvie/depart)
     * @return string
     */
    @Override
    public String toFormattedText() {
        Instant start = this.getStart();
        Instant end = this.getEnd();
        String visit;
        String formatted_visit;
        if (end == null) { //TODO: always false! Make sure this goes out correctly
            visit="arrive, %d, %s";   //arrive, id, arrive time
            formatted_visit=String.format(visit, this.getID(), format_time(this.getStart()));
        }else{
            visit= "depart, %d, %s";                   //depart, id, end-time, duration
            formatted_visit=String.format(visit, this.getID(),format_time(end), formatDuration(end, start));
        }

        return formatted_visit;
    }

    /**
     * gets the string representation of the visit duration (hr:min:sec)
     * @param end- end time
     * @param start- start time
     * @return string duration
     */
    //Probably not worth making public just yet, we might want to move it after all
    private String formatDuration(Instant end, Instant start){
        Duration dur= Duration.between(start, end);
        /*int hours= dur.toHoursPart();
        int mins= dur.toMinutesPart();  //TODO: Fix
        int seconds= dur.toSecondsPart();
        return String.format("%d:%d:%d", hours, mins, seconds);*/
        return dur.toString();
    }

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

        public static DataField<? super Object>[] values() {
            return new DataField[] { ID, VISITOR, START, END };
        }

        static {
            ID = DataField.makeIDField(Checkout.class);
            VISITOR = DataField.buildFromState(Visitor.class, "visitor", Visitor.Field.ID, FM_IMMUTABLE, FM_KEY);
            START = DataField.buildSimple(Instant.class, "start", FM_IMMUTABLE);
            END = DataField.buildSimple(Instant.class, "end", FM_IMMUTABLE);
            VALUES = Author.Field.values();
        }
    }

    Visit(DataStorage storage, long id, StateBuilder<Visit> builder) {
        super(storage, id, builder);
    }

    public Visit(DataStorage storage, ResultSet sql) throws SQLException {
        super(storage, sql);
    }

    public Visit(DataStorage storage, Map<String, Object> file) {
        super(storage, file);
    }

    @Override
    protected DataField<? super Object>[] getFieldUnsafe() {
        return Field.VALUES;
    }

    public Visitor getVisitor() {
        return Field.VISITOR.get(this);
    }

    public Instant getStart() {
        return Field.START.get(this);
    }

    public Instant getEnd() {
        return Field.END.get(this);
    }

    @Override
    public DataField<Long> getIDField() {
        return Field.ID;
    }

    @Override
    public DataField<? super Object>[] getFields() {
        return Field.values();
    }

    @Override
    public Type getType() {
        return StateType.VISIT;
    }

    public static Builder create() {
        return new Builder();
        //TODO: replace with below once command code is fixed
        //return StateBuilder.of(Visit::new, StateType.VISIT, Field.VALUES);
    }

    @Deprecated
    public static class Builder extends StateBuilder<Visit> {

        private Builder() {
            super(StateType.VISIT, Field.VALUES);
        }

        public Builder start(Instant start) {
            this.setValue(Field.START, start);
            return this;
        }

        public Builder end(Instant end) {
            this.setValue(Field.END, end);
            return this;
        }

        public Builder visitor(Visitor visitor) {
            this.setValue(Checkout.Field.VISITOR, visitor);
            return this;
        }

        @Override
        protected Visit buildObj(DataStorage storage, long id) {
            return new Visit(storage, id, this);
        }
    }
}
