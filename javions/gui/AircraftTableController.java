package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS;

/**
 * Manages the aircraft states that has to be shown on the table
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class AircraftTableController {
    private static final int ICAO_COLUMN_SIZE = 60;
    private static final int CALLSIGN_COLUMN_SIZE = 70;
    private static final int REGISTRATION_COLUM_SIZE = 90;
    private static final int MODEL_COLUMN_SIZE = 230;
    private static final int TYPE_COLUMN_SIZE = 50;
    private static final int DESCRIPTION_COLUMN_SIZE = 70;
    private static final int NUMERIC_COLUMN_SIZE = 85;
    private static final int TWO_CLICKS = 2;

    private final ObservableSet<ObservableAircraftState> observableAircraftStates;
    private final ObjectProperty<ObservableAircraftState> aircraftStateProperty;
    private final TableView<ObservableAircraftState> tableView;
    private Consumer<ObservableAircraftState> consumer;


    /**
     * Constructs an aircraft table controller
     *
     * @param observableAircraftStates (an observable but not modifiable) of the states of the aircraft which must
     *                                 appear on the view—and which comes of course from a state manager,
     * @param aircraftStateProperty    a JavaFX property containing the state of the selected aircraft,
     *                                 the contents of which can be null when no aircraft is selected—a property that is
     *                                 intended to be the same as the one used by the aircraft view.
     */
    public AircraftTableController(
            ObservableSet<ObservableAircraftState> observableAircraftStates,
            ObjectProperty<ObservableAircraftState> aircraftStateProperty) {
        this.observableAircraftStates = observableAircraftStates;
        this.aircraftStateProperty = aircraftStateProperty;
        this.tableView = new TableView<>();

        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.getStylesheets().add("table.css");
        tableView.setTableMenuButtonVisible(true);

        addListeners();
        addTextualColumns();
        addNumericColumns();
    }

    /**
     * Returns the tableView
     *
     * @return the tableView
     */
    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    /**
     * Takes a value of type Consumer<ObservableAircraftState> as an argument, and calls its accept method when
     * a double click is performed on the table and an aircraft is currently selected, passing it the state of
     * this aircraft as an argument.
     *
     * @param observableAircraftStateConsumer the consumer
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> observableAircraftStateConsumer) {
        this.consumer = observableAircraftStateConsumer;
    }

    private void addTextualColumns() {
        tableView.getColumns().add(
                createTextualColumn("OACI",
                        f -> new ReadOnlyObjectWrapper<>(f.getIcaoAddress()).map(IcaoAddress::string),
                        ICAO_COLUMN_SIZE));
        tableView.getColumns().add(
                createTextualColumn("Indicatif",
                        f -> f.callSignProperty().map(CallSign::string),
                        CALLSIGN_COLUMN_SIZE));
        tableView.getColumns().add(
                createTextualColumn("Immatriculation",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(c -> c.registration().string()),
                        REGISTRATION_COLUM_SIZE));
        tableView.getColumns().add(
                createTextualColumn("Modèle",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(AircraftData::model),
                        MODEL_COLUMN_SIZE));
        tableView.getColumns().add(
                createTextualColumn("Type",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(c -> c.typeDesignator().string()),
                        TYPE_COLUMN_SIZE));
        tableView.getColumns().add(
                createTextualColumn("Description",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(c -> c.description().string()),
                        DESCRIPTION_COLUMN_SIZE));
    }

    private void addNumericColumns() {
        tableView.getColumns().add(
                createNumericColumn("Longitude(°)",
                        f -> Bindings.createDoubleBinding(() -> f.getPosition().longitude(), f.positionProperty()),
                        Units.Angle.DEGREE, 4, 4));
        tableView.getColumns().add(
                createNumericColumn("Latitude(°)",
                        f -> Bindings.createDoubleBinding(() -> f.getPosition().latitude(), f.positionProperty()),
                        Units.Angle.DEGREE, 4, 4));
        tableView.getColumns().add(
                createNumericColumn("Altitude(m)",
                        ObservableAircraftState::altitudeProperty, Units.Length.METER, 0, 0));
        tableView.getColumns().add(
                createNumericColumn("Vitesse(km/h)", ObservableAircraftState::velocityProperty,
                        Units.Speed.KILOMETER_PER_HOUR, 0, 0));
    }

    private TableColumn<ObservableAircraftState, String> createNumericColumn(
            String title, Function<ObservableAircraftState, DoubleExpression> function,
            double unit, int maxDigits, int minDigits) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.getStyleClass().add("numeric");
        column.setPrefWidth(NUMERIC_COLUMN_SIZE);

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(maxDigits);
        numberFormat.setMinimumFractionDigits(minDigits);

        column.setCellValueFactory(e ->
                function.apply(e.getValue()).map(c ->
                        Double.isNaN(c.doubleValue()) ? "" : numberFormat.format(Units.convertTo(c.doubleValue(), unit))));
        column.setComparator((x, y) -> {
            if (x.isEmpty() || y.isEmpty()) return x.compareTo(y);
            try {
                double num1 = numberFormat.parse(x).doubleValue();
                double num2 = numberFormat.parse(y).doubleValue();
                return Double.compare(num1, num2);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        return column;
    }

    private TableColumn<ObservableAircraftState, String> createTextualColumn(
            String title, Function<ObservableAircraftState, ObservableValue<String>> function, int width) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(c -> function.apply(c.getValue()));
        return column;
    }

    private void addListeners() {

        tableView.setOnMouseClicked(e -> {
            if (Objects.nonNull(consumer) && Objects.nonNull(tableView.getSelectionModel().getSelectedItem())
                    && e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == TWO_CLICKS) {
                consumer.accept(tableView.getSelectionModel().getSelectedItem());
            }
        });

        observableAircraftStates.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                    if (change.wasAdded()) {
                        tableView.getItems().add(change.getElementAdded());
                        tableView.sort();
                    } else if (change.wasRemoved()) {
                        tableView.getItems().remove(change.getElementRemoved());
                    }
                });

        aircraftStateProperty.addListener((e, p, q) -> {
            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), q)) tableView.scrollTo(q);
            tableView.getSelectionModel().select(q);
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((value, oldValue, newValue) ->
                aircraftStateProperty.set(newValue));
    }
}
