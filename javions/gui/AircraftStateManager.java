package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static javafx.collections.FXCollections.observableSet;
import static javafx.collections.FXCollections.unmodifiableObservableSet;

/**
 * Updates the states of a set of aircraft based on messages received from them
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class AircraftStateManager {
    private static final double ONE_MINUTE_IN_NANOSECONDS = 6e+10;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObservableSet<ObservableAircraftState> unmodifiableStates;
    private final AircraftDatabase database;
    private long lastTimeStampNs;

    /**
     * Returns an unmodifiable and observable set of aircraft states
     *
     * @return states of aircraft
     */
    public ObservableSet<ObservableAircraftState> states() {
        return unmodifiableStates;
    }

    /**
     * Constructs an aircraftStateManager with a database
     *
     * @param database of the aircraft
     * @throws NullPointerException if database is null
     */
    public AircraftStateManager(AircraftDatabase database) {
        this.database = Objects.requireNonNull(database);
        this.table = new HashMap<>();
        states = observableSet();
        unmodifiableStates = unmodifiableObservableSet(states);
    }

    /**
     * Updates the aircraft state of the aircraft that sent the message
     *
     * @param message sent
     * @throws IOException if an error occurs while reading a file
     */
    public void updateWithMessage(Message message) throws IOException {

        IcaoAddress icaoAddress = message.icaoAddress();
        AircraftData aircraftData = database.get(icaoAddress);
        if (table.containsKey(icaoAddress)) {
            table.get(icaoAddress).update(message);
        } else {
            table.put(icaoAddress, new AircraftStateAccumulator<>(
                    new ObservableAircraftState(icaoAddress, aircraftData)));
            table.get(icaoAddress).update(message);
        }
        AircraftStateAccumulator<ObservableAircraftState> aircraftStateAircraftStateAccumulator = table.get(icaoAddress);
        if (aircraftStateAircraftStateAccumulator.stateSetter().getPosition() != null)
            states.add(aircraftStateAircraftStateAccumulator.stateSetter());
        this.lastTimeStampNs = message.timeStampNs();
    }

    /**
     * Removes all aircraft observable states corresponding to aircraft from which no message has been received
     * in the minute preceding the reception of the last message passed to updateWithMessage
     */
    public void purge() {
        states.removeIf(e -> {
            if (lastTimeStampNs - e.getLastMessageTimeStampNs() >= ONE_MINUTE_IN_NANOSECONDS) {
                table.remove(e.getIcaoAddress());
                return true;
            } else return false;
        });
    }
}
