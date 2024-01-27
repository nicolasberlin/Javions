package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * Represents the state of an aircraft which is observed
 */

public final class ObservableAircraftState implements AircraftStateSetter {
    private final AircraftData aircraftData;
    private final IcaoAddress icaoAddress;
    private final LongProperty lastMessageTimeStampNsProperty = new SimpleLongProperty();
    private final IntegerProperty categoryProperty = new SimpleIntegerProperty();
    private final ObjectProperty<CallSign> callSignProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<GeoPos> positionProperty = new SimpleObjectProperty<>();
    private final DoubleProperty altitudeProperty = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty velocityProperty = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty trackOrHeadingProperty = new SimpleDoubleProperty(Double.NaN);
    private final ObservableList<AirbornePos> trajectory = FXCollections.observableArrayList();
    private final ObservableList<AirbornePos> unmodifiedTrajectory = FXCollections.unmodifiableObservableList(trajectory);
    private long lastMessageAddedInTrajectory;

    /**
     * Constructs an observable and updatable aircraft state with an ICAO address and an aircraftdata
     *
     * @param icaoAddress  of the aircraft
     * @param aircraftData of the aircraft
     * @throws NullPointerException if icaoAddress is null
     */

    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        Objects.requireNonNull(icaoAddress);
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;
    }

    /**
     * Returns the icaoAddress
     *
     * @return the icaoAddress
     */

    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }

    /**
     * Represents an airborne position in the map
     *
     * @param position of the aircraft
     * @param altitude of the aircraft
     */
    public record AirbornePos(GeoPos position, double altitude) {
    }

    /**
     * Returns the aircraft data
     *
     * @return the aircraft data
     */

    public AircraftData getAircraftData() {
        return aircraftData;
    }

    /**
     * Returns the property of lastMessageTimeStampNs
     *
     * @return the property of lastMessageTimeStampNs
     */

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNsProperty;
    }

    /**
     * Returns the property of category
     *
     * @return the property of category
     */

    public ReadOnlyIntegerProperty categoryProperty() {
        return categoryProperty;
    }

    /**
     * Returns the property of position
     *
     * @return the property of position
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return positionProperty;
    }

    /**
     * Returns the property of callSign
     *
     * @return the property of callSign
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSignProperty;
    }

    /**
     * Returns the property of altitude
     *
     * @return the property of altitude
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitudeProperty;
    }

    /**
     * Returns the property of velocity
     *
     * @return the property of velocity
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocityProperty;
    }

    /**
     * Returns the property of trackOrHeading
     *
     * @return the property of trackOrHeading
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeadingProperty;
    }

    /**
     * Returns lastMessageTimeStampNs
     *
     * @return lastMessageTimeStampNs
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNsProperty.get();
    }

    /**
     * Returns the category of the aircraft
     *
     * @return category of the aircraft
     */

    public int getCategory() {
        return categoryProperty.get();
    }

    /**
     * Returns the callSign of the aircraft
     *
     * @return the callSign of the aircraft
     */
    public CallSign getCallSign() {
        return callSignProperty.get();
    }

    /**
     * Returns the aircraft's position
     *
     * @return the aircraft's position
     */

    public GeoPos getPosition() {
        return positionProperty.get();
    }

    /**
     * Returns an unmodifiable list of the trajectory
     *
     * @return unmodifiable list
     */

    public ObservableList<AirbornePos> getTrajectory() {
        return unmodifiedTrajectory;
    }

    /**
     * Returns the altitude of the aircraft
     *
     * @return the altitude of the aircraft
     */
    public double getAltitude() {
        return altitudeProperty.get();
    }

    /**
     * Returns the velocity of the aircraft
     *
     * @return the velocity of the aircraft
     */

    public double getVelocity() {
        return velocityProperty.get();
    }

    /**
     * Returns the trackOrHeading (direction) of the aircraft
     *
     * @return the trackOrHeading
     */

    public double getTrackOrHeading() {
        return trackOrHeadingProperty.get();
    }

    /**
     * Sets the timestamp in the property
     *
     * @param timeStampsNs the new timestamp of the message
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampsNs) {
        lastMessageTimeStampNsProperty.set(timeStampsNs);
    }

    /**
     * Sets the category in the property
     *
     * @param category the new category of the aircraft
     */
    @Override
    public void setCategory(int category) {
        categoryProperty.set(category);
    }

    /**
     * Sets the call sign in the property
     *
     * @param callSign the new call sign of the aircraft
     */
    @Override
    public void setCallSign(CallSign callSign) {
        callSignProperty.set(callSign);
    }

    /**
     * Sets tbe position. If the altitude is known, then the pair (current position, current altitude) is added
     * to the trajectory.
     *
     * @param position the new position of the aircraft
     */
    @Override
    public void setPosition(GeoPos position) {

        if (!Double.isNaN(getAltitude())) {
            trajectory.add(new AirbornePos(position, getAltitude()));
            lastMessageAddedInTrajectory = getLastMessageTimeStampNs();
        }
        positionProperty.set(position);
    }

    /**
     * Sets the altitude. If the position is known then if the trajectory is empty, then the pair
     * (current position, current altitude) is added to the trajectory, or if the timestamp of the
     * current message is identical to that of the message which caused the last addition to the
     * trajectory,then its last element is replaced by the pair (current position, current altitude)
     *
     * @param altitude the new altitude of the aircraft
     */

    @Override
    public void setAltitude(double altitude) {
        if (Objects.nonNull(getPosition())) {
            if (trajectory.isEmpty()) {
                trajectory.add(new AirbornePos(getPosition(), altitude));
                lastMessageAddedInTrajectory = getLastMessageTimeStampNs();
            } else if (getLastMessageTimeStampNs() == lastMessageAddedInTrajectory) {
                trajectory.set(trajectory.size() - 1, new AirbornePos(getPosition(), altitude));
            }
        }
        altitudeProperty.set(altitude);
    }

    /**
     * Sets the velocity
     *
     * @param velocity the new velocity of the aircraft
     */
    @Override
    public void setVelocity(double velocity) {
        velocityProperty.set(velocity);
    }

    /**
     * Sets the direction
     *
     * @param trackOrHeading new direction of the aircraft
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        trackOrHeadingProperty.set(trackOrHeading);
    }
}
