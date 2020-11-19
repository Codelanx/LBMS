package edu.rit.codelanx.network.client.gui.menu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateTimePicker extends DatePicker {

    private final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:");
    private final ObjectProperty<LocalDateTime> value = new SimpleObjectProperty<>(null, "datetime");

    public DateTimePicker(LocalDate date, LocalTime time) {
        super(date);
        this.value.set(date.atTime(time));
        this.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate object) {
                return null;
            }

            @Override
            public LocalDate fromString(String string) {
                return null;
            }
        });
        this.getEditor().addEventHandler(ActionEvent.ACTION, event -> {

        });
        this.valueProperty().addListener((arg, old, next) -> {
            LocalDateTime curr = Optional.ofNullable(this.value.get()).orElseGet(LocalDateTime::now);
            this.value.set(next == null ? null : next.atTime(curr.toLocalTime()));
        });
        this.dateTimeProperty().addListener((arg, old, next) -> {
            this.valueProperty().set(next == null ? null : next.toLocalDate());
        });
        this.getEditor().focusedProperty().addListener((arg, old, next) -> {
            if (!next) {
                //fake an event to force hover to update
                this.getEditor().fireEvent(new KeyEvent(this.getEditor(), this.getEditor(), KeyEvent.KEY_PRESSED, null, null, KeyCode.ENTER, false, false, false, false));
            }
        });
    }

    public ObjectProperty<LocalDateTime> dateTimeProperty() {
        return this.value;
    }

}
