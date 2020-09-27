package edu.rit.codelanx.data.types;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.ResultRow;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.ResultantState;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StateBuilder;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Visit extends ResultantState implements FileSerializable {

    private final long visitorID;
    private final Instant start;
    private final Instant end;

    private Visit(long id, Builder builder) {
        super(id);
        this.visitorID = builder.visitorID;
        this.start = builder.start;
        this.end = builder.end;
    }

    public Visit(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.visitorID = sql.getLong("visitor");
        this.start = sql.getTimestamp("start").toInstant();
        this.end = sql.getTimestamp("end").toInstant();
    }

    public Visit(Map<String, Object> file) {
        super((Long) file.get("id"));
        this.visitorID = (Long) file.get("visitor");
        this.start = Instant.ofEpochMilli((Long) file.get("start"));
        this.end = Instant.ofEpochMilli((Long) file.get("end"));
    }

    public Instant getStart() {
        return this.start;
    }

    public Instant getEnd() {
        return this.end;
    }

    @Override
    public State.Type getType() {
        return State.Type.VISIT;
    }

    /**
     * String representation of a visit (arrvie/depart)
     * @return string
     */
    @Override
    public String toFormattedText() {
        String visit;
        String formatted_visit;
        if (this.getEnd().equals(null)){
            visit="arrive, %d, %s";   //arrive, id, arrive time
            formatted_visit=String.format(visit, this.getID(), format_time(this.getStart()));
        }else{
            visit= "depart, %d, %s";                   //depart, id, end-time, duration
            formatted_visit=String.format(visit, this.getID(),format_time(this.end), getDuration(this.end, this.start));
        }

        return formatted_visit;
    }

    /**
     * gets the string representation of the visit duration (hr:min:sec)
     * @param end- end time
     * @param start- start time
     * @return string duration
     */
    public String getDuration(Instant end, Instant start){
        Duration dur= Duration.between(start, end);
        int hours= dur.toHoursPart();
        int mins= dur.toMinutesPart();
        int seconds= dur.toSecondsPart();
        return String.format("%d:%d:%d", hours, mins, seconds);
    }

    /**
     * format time (type instant) into a string
     *
     * @param time- to be formatted
     * @return string representation of time
     */
    public String format_time(Instant time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.US).withZone(ZoneId.systemDefault());
        String str_time = formatter.format(time);
        return str_time;
    }

    public static Builder create(DataStorage storage) {
        return new Builder(storage);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.id);
        back.put("start", this.start.toEpochMilli());
        back.put("end", this.end.toEpochMilli());
        return back;
    }

    public static class Builder extends StateBuilder<Visit> {

        private Instant start;
        private Instant end;
        private Long visitorID;

        private Builder(DataStorage storage) {
            super(storage);
        }

        public Builder start(Instant start) {
            this.start = start;
            return this;
        }

        public Builder end(Instant end) {
            this.end = end;
            return this;
        }

        public Builder visitor(Visitor visitor) {
            this.visitorID = visitor.getID();
            return this;
        }

        @Override
        public boolean isValid() {
            return this.start != null && this.end != null && this.visitorID != null;
        }

        @Override
        public Object[] asSQLArguments() {
            return new Object[] {this.visitorID, this.start.toEpochMilli(), this.end.toEpochMilli()};
        }

        @Override
        protected Visit buildObj(long id) {
            return new Visit(id, this);
        }
    }
}
