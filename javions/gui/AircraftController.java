package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.beans.binding.Bindings;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ch.epfl.javions.WebMercator.x;
import static ch.epfl.javions.WebMercator.y;
import static javafx.scene.paint.CycleMethod.NO_CYCLE;


/**
 * Manages the view of aircraft on the world map.
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class AircraftController {

    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftState;
    private final Pane pane = new Pane();
    private final static int ZOOM_LEVEL = 11;
    private final static int HIGHEST_ALTITUDE = 12000;

    private final static String EMPTY_STRING = "";

    /**
     * Constructs a view of aircraft on world map
     *
     * @param mapParameters         parameters of the visible map
     * @param aircraftStateSet      set of the aircraft's states which should appear on the view
     * @param selectedAircraftState contains the state of the selected aircraft, it can be null
     *                              if no aircraft is selected
     * @throws IllegalArgumentException when aircraft state's is an empty set
     */
    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStateSet,
                              ObjectProperty<ObservableAircraftState> selectedAircraftState) {

        Preconditions.checkArgument(aircraftStateSet.isEmpty());
        this.mapParameters = mapParameters;
        this.selectedAircraftState = selectedAircraftState;
        pane.getStylesheets().add("aircraft.css");
        pane.setPickOnBounds(false);
        aircraftStateSet.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                ObservableAircraftState addedAircraft = change.getElementAdded();
                Group aircraft = annotatedAircraft(addedAircraft);
                pane.getChildren().add(aircraft);
            }
            if (change.wasRemoved()) {
                ObservableAircraftState removedAircraft = change.getElementRemoved();
                pane.getChildren().removeIf(child -> idAircraft(removedAircraft).equals(child.getId()));
            }
        });

    }

    /**
     * Returns the pane on which the aircraft is shown.
     *
     * @return the pane on which there is a world map with aircraft to be shown.
     */
    public Pane pane() {
        return pane;
    }

    private Group annotatedAircraft(ObservableAircraftState aircraftState) {
        Group aircraftGroup = new Group();
        String icaoAddress = String.valueOf(aircraftState.getIcaoAddress());
        aircraftGroup.setId(icaoAddress);
        Group trajectoryGroup = trajectory(aircraftState);
        Group labelAndIcon = labelAndIconGroup(aircraftState);
        aircraftGroup.getChildren().addAll(trajectoryGroup, labelAndIcon);
        aircraftGroup.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        return aircraftGroup;
    }

    private Group trajectory(ObservableAircraftState aircraftState) {
        Group groupTrajectory = new Group();
        groupTrajectory.getStyleClass().add("trajectory");
        InvalidationListener trajectoryListener = change -> {
            if (aircraftState.equals(selectedAircraftState.getValue()))
                drawTrajectory(groupTrajectory, aircraftState);
        };

        InvalidationListener zoomListener = change -> drawTrajectory(groupTrajectory, aircraftState);
        groupTrajectory.visibleProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                drawTrajectory(groupTrajectory, aircraftState);
                aircraftState.getTrajectory().addListener(trajectoryListener);
                mapParameters.zoomProperty().addListener(zoomListener);

            } else {
                aircraftState.getTrajectory().removeListener(trajectoryListener);
                mapParameters.zoomProperty().removeListener(zoomListener);
                groupTrajectory.getChildren().clear();
            }
        });


        groupTrajectory.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                -mapParameters.getMinX(), mapParameters.minXProperty()
        ));
        groupTrajectory.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                -mapParameters.getMinY(), mapParameters.minYProperty()
        ));

        groupTrajectory.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                aircraftState.equals(selectedAircraftState.get()), selectedAircraftState));
        return groupTrajectory;

    }

    private void drawTrajectory(Group groupTrajectory, ObservableAircraftState aircraftState) {

        List<ObservableAircraftState.AirbornePos> airbornePos = aircraftState.getTrajectory();

        List<Line> lines = new ArrayList<>();
        groupTrajectory.getChildren().clear();

        List<ObservableAircraftState.AirbornePos> airbornePositionList = airbornePos.subList(1, airbornePos.size());
        ObservableAircraftState.AirbornePos firstElement = airbornePos.get(0);

        for (ObservableAircraftState.AirbornePos position : airbornePositionList) {

            int zoom = mapParameters.getZoom();

            GeoPos firstPosition = firstElement.position();
            double startX = x(zoom, firstPosition.longitude());
            double startY = y(zoom, firstPosition.latitude());

            GeoPos nextPosition = position.position();
            double endX = x(zoom, nextPosition.longitude());
            double endY = y(zoom, nextPosition.latitude());

            Line line = new Line(startX, startY, endX, endY);

            colorTrajectory(firstElement, position, line);

            lines.add(line);

            firstElement = position;
        }
        groupTrajectory.getChildren().addAll(lines);
    }


    private Group labelAndIconGroup(ObservableAircraftState aircraftState) {
        Group labelAndIconGroup = new Group();
        labelAndIconGroup.getChildren().addAll(label(aircraftState), icon(aircraftState));

        labelAndIconGroup.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
            double longitude = aircraftState.getPosition().longitude();
            double longitudeInPixel = x(mapParameters.getZoom(), longitude);
            return longitudeInPixel - mapParameters.getMinX();
        }, mapParameters.minXProperty(), mapParameters.zoomProperty(), aircraftState.positionProperty()));

        labelAndIconGroup.layoutYProperty().bind(Bindings.createDoubleBinding(() -> {
                    double latitude = aircraftState.getPosition().latitude();
                    double latitudeInPixel = y(mapParameters.getZoom(), latitude);
                    return latitudeInPixel - mapParameters.getMinY();

                },
                mapParameters.zoomProperty(), mapParameters.minYProperty(), aircraftState.positionProperty()));
        return labelAndIconGroup;
    }

    private Group label(ObservableAircraftState aircraftState) {
        Group labelGroup = new Group();
        labelGroup.getStyleClass().add("label");
        Text text = new Text();

        text.textProperty().bind(Bindings.createStringBinding(() -> {

            String textLabel = "\n" + verificationOfVelocity(aircraftState) + "\u2002"
                    + String.format("%.0f m", aircraftState.getAltitude());
            AircraftData data = aircraftState.getAircraftData();
            if (data != null) {

                AircraftRegistration registration = data.registration();
                if (registration != null) {
                    return registration.string() + textLabel;
                }

                ReadOnlyObjectProperty<CallSign> callSignProperty = aircraftState.callSignProperty();
                CallSign callSign = aircraftState.getCallSign();
                if (callSignProperty != null) {
                    return callSign + textLabel;
                }

                IcaoAddress icaoAddress = aircraftState.getIcaoAddress();
                return icaoAddress.string() + textLabel;
            }
            return "" + textLabel;
        }, aircraftState.altitudeProperty(), aircraftState.velocityProperty(), aircraftState.callSignProperty()));


        Rectangle rectangleLabel = new Rectangle();
        rectangleLabel.widthProperty().bind(
                text.layoutBoundsProperty().map(labelWidth -> labelWidth.getWidth() + 4));
        rectangleLabel.heightProperty().bind(
                text.layoutBoundsProperty().map(labelHeight -> labelHeight.getHeight() + 4));

        labelGroup.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                        mapParameters.getZoom() >= ZOOM_LEVEL || aircraftState.equals(selectedAircraftState.get()),
                mapParameters.zoomProperty(), selectedAircraftState));

        labelGroup.getChildren().addAll(rectangleLabel, text);

        return labelGroup;
    }

    private SVGPath icon(ObservableAircraftState aircraftState) {
        SVGPath path = new SVGPath();

        AircraftData data = aircraftState.getAircraftData();
        AircraftTypeDesignator typeDesignator;
        AircraftDescription description;
        WakeTurbulenceCategory wakeTurbulenceCategory;
        if (Objects.isNull(data)) {
            typeDesignator = new AircraftTypeDesignator(EMPTY_STRING);
            description = new AircraftDescription(EMPTY_STRING);
            wakeTurbulenceCategory = WakeTurbulenceCategory.UNKNOWN;

        } else {
            typeDesignator = data.typeDesignator();
            description = data.description();
            wakeTurbulenceCategory = data.wakeTurbulenceCategory();
        }

        ObjectProperty<AircraftIcon> iconObjectProperty = new SimpleObjectProperty<>();
        iconObjectProperty.bind(aircraftState.categoryProperty().map(category ->
                AircraftIcon.iconFor(typeDesignator, description, category.intValue(), wakeTurbulenceCategory)));

        path.contentProperty().bind(iconObjectProperty.map(AircraftIcon::svgPath));

        path.rotateProperty().bind(Bindings.createDoubleBinding(() -> {
            AircraftIcon iconProperty = iconObjectProperty.get();
            if (iconProperty.canRotate()) {
                double trackOrHeading = aircraftState.getTrackOrHeading();
                return Units.convertTo(trackOrHeading, Units.Angle.DEGREE);
            } else
                return 0d;
        }, iconObjectProperty, aircraftState.trackOrHeadingProperty()));


        path.getStyleClass().add("aircraft");
        path.visibleProperty().set(true);

        path.fillProperty().bind(aircraftState.altitudeProperty().map(altitude ->
                getColorForCalculatedAltitude(altitude.doubleValue())));

        path.setOnMouseClicked(event ->
                selectedAircraftState.set(aircraftState)
        );
        return path;
    }

    private String idAircraft(ObservableAircraftState aircraftState) {
        IcaoAddress icaoAddress = aircraftState.getIcaoAddress();
        return String.valueOf(icaoAddress);
    }

    private String verificationOfVelocity(ObservableAircraftState aircraftState) {
        double velocity = aircraftState.getVelocity();
        if (Double.isNaN(velocity)) {
            return "? km/h";
        }
        return String.format("%.0f km/h", Units.convertTo(velocity, Units.Speed.KILOMETER_PER_HOUR));
    }

    private void colorTrajectory(ObservableAircraftState.AirbornePos position,
                                 ObservableAircraftState.AirbornePos nextPosition, Line line) {

        double currentAltitude = position.altitude();
        double nextAltitude = nextPosition.altitude();
        Color firstColor = getColorForCalculatedAltitude(currentAltitude);
        if (currentAltitude == nextAltitude) {
            Color colorValue = getColorForCalculatedAltitude(nextAltitude);
            line.setStroke(colorValue);
        } else {
            Color secondColor = getColorForCalculatedAltitude(nextAltitude);
            Stop s1 = new Stop(0, firstColor);
            Stop s2 = new Stop(1, secondColor);
            LinearGradient lineGradient = new LinearGradient(0, 0, 1, 0,
                    true, NO_CYCLE, s1, s2);
            line.setStroke(lineGradient);
        }
    }

    private Color getColorForCalculatedAltitude(double altitude) {
        double calculatedAltitude = Math.cbrt(altitude / HIGHEST_ALTITUDE);
        return ColorRamp.PLASMA.at(calculatedAltitude);
    }

}
