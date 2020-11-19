package edu.rit.codelanx.network.client.gui.menu;

import com.codelanx.commons.util.InputOutput;
import edu.rit.codelanx.cmd.text.TextCommand;
import edu.rit.codelanx.network.client.gui.GuiClient;
import edu.rit.codelanx.network.client.gui.views.LoginView;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FileMenu extends Menu {

    public FileMenu(GuiClient client) {
        super("File");
        List<MenuItem> items = this.getItems();
        MenuItem advTime = new MenuItem("Advance Time");
        MenuItem genReport = new MenuItem("Generate Report");
        MenuItem logout = new MenuItem("Logout");
        logout.disableProperty().bind(client.loginIDProperty().lessThanOrEqualTo(0L));
        advTime.addEventHandler(ActionEvent.ACTION, event -> this.advanceTime(client));
        genReport.addEventHandler(ActionEvent.ACTION, event -> this.generateReport(client));
        logout.addEventHandler(ActionEvent.ACTION, event -> {
            //disposes all the old info, too
            client.setLoginID(GuiClient.LOGOUT_ID);
            client.setRoot(new LoginView(client));
        });

        Stream.of(advTime, genReport, logout).forEach(items::add);

        /*

         * AdvanceCommand - menu opt
         * DatetimeCommand - static control?
         * DepartCommand - main pane exit?
         * ReportCommand - menu opt
         * ReturnCommand - button
         * SearchCommand - scene            TODO: BookView
         */

        /**
         *
         *          * AdvanceCommand - menu opt
         *          * ArriveCommand - main pane?       TODO: StartView, LoginView
         *          * BorrowCommand - button
         *          * BorrowedCommand - scene          TODO: BookView
         *          * BuyCommand - button
         *          * DatetimeCommand - static control?
         *          * DepartCommand - main pane exit?
         *          * InfoCommand - scene              TODO: BookView
         *          * PayCommand - prompt/input/button
         *          * RegisterCommand - main pane?     TODO: StartView, RegView
         *          * ReportCommand - menu opt
         *          * ReturnCommand - button
         *          * SearchCommand - scene            TODO: BookView
         */
    }

    private static final long MAX_SECONDS = TimeUnit.DAYS.toSeconds(7);

    private void advanceTime(GuiClient client) {


        String resp = client.sendIfConnected("datetime;");
        if (resp == null) {
            new Alert(Alert.AlertType.ERROR, "Not connected to server").show();
            return;
        }
        String[] data = resp.split(TextCommand.TOKEN_DELIMITER);
        LocalDate date = TextCommand.DATE_FORMAT.parse(data[1], LocalDate::from);
        LocalTime time = TextCommand.TIME_OF_DAY_FORMAT.parse(data[2], LocalTime::from);
        ZoneOffset def = ZoneOffset.of(ZoneId.systemDefault().getId());
        Instant now = date.atTime(time).toInstant(def);

        DateTimePicker picker = new DateTimePicker(date, time);
        picker.setChronology(date.getChronology());
        /*picker.addEventFilter(ActionEvent.ACTION, (arg, old, next) -> {
            Duration d = Duration.between(now, null);//next);
            if (d.isNegative() || d.getSeconds() > MAX_SECONDS) {
                //TODO: error field

            }
        });*/

        //TODO: New advance time window

            /*


            .ifPresent(days -> {
                        String resp = client.sendIfConnected("advance," + days + ";");
                        if (resp == null) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Lost connection to server");
                            alert.show();
                            return;
                        }
                        switch (resp) {
                            case "advance,success;":
                                //TODO: Update time display
                                break;
                            default:
                                Alert alert = new Alert(Alert.AlertType.ERROR, resp);
                                alert.show();
                                break;
                        }
                    });

             */
    }

    private void generateReport(GuiClient client) {
        TextInputDialog txt = new TextInputDialog();
        Text error = new Text();
        error.getStyleClass().add("error");
        txt.getDialogPane().setExpandableContent(error);
        txt.getEditor().focusedProperty().addListener((arg, old, next) -> {
            if (!next) {
                CharSequence val = txt.getEditor().getCharacters();
                if (val.length() <= 1) {
                    return;
                }
                int diff = val.charAt(0) - '0';
                if (diff < 0 || diff > 7) {
                    txt.getEditor().getStyleClass().add("error");
                    error.setText("Value must be between 0 and 7 (inclusive)");
                } else {
                    txt.getEditor().getStyleClass().remove("error");
                    error.setText("");
                }
            }
        });
        ButtonType bt = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        txt.getDialogPane().getButtonTypes().add(bt);
        txt.setHeaderText("Enter number of days [0-7]:");
        txt.showAndWait().flatMap(InputOutput::parseLong)
                .ifPresent(days -> {
                    String resp = client.sendIfConnected("report," + days + ";");
                    if (resp == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Lost connection to server");
                        alert.show();
                        return;
                    }
                    resp = resp.substring("report,".length());
                    int spl = resp.indexOf(',');
                    resp = resp.substring(0, spl) + resp.substring(spl + 1);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, resp);
                    alert.show();
                });
    }
}
