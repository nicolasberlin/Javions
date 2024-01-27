package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.util.Objects;

/**
 * Represents an aircraft state accumulator.
 * An object that represents a modifiable state of an aircraft of type AircraftStateSetter.
 * It has methods to update the state of the aircraft and memorises the last even and odd message to find its position
 *
 * @author Ünlüer Asli(329696)
 * @author Berlin Nicolas(355535)
 */

public final class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final static double PRECISION = 1e+10;
    private AirbornePositionMessage lastEvenMessage;
    private AirbornePositionMessage lastOddMessage;
    private final T stateSetter;
    private final static int EVEN = 0;
    private final static int ODD = 1;


    /**
     * Constructs an AircraftStateAccumulator given the state declared
     *
     * @param stateSetter of the aircraft
     * @throws NullPointerException if the stateSetter is null
     */
    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    /**
     * Returns the stateSetter
     *
     * @return stateSetter
     */
    public T stateSetter() {
        return stateSetter;
    }

    /**
     * Updates the modifiable state given the message
     *
     * @param message of the aircraft
     */
    public void update(Message message) {
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {
            case AircraftIdentificationMessage aim -> {
                stateSetter.setCategory(aim.category());
                stateSetter.setCallSign(aim.callSign());
            }
            case AirbornePositionMessage apm -> {
                stateSetter.setAltitude(apm.altitude());
                if (apm.parity() == ODD) {
                    lastOddMessage = apm;
                } else {
                    lastEvenMessage = apm;
                }
                if (lastEvenMessage != null && lastOddMessage != null) {
                    if (apm.parity() == ODD && apm.timeStampNs() - lastEvenMessage.timeStampNs() <= PRECISION) {
                        double x0 = lastEvenMessage.x();
                        double y0 = lastEvenMessage.y();
                        double x1 = apm.x();
                        double y1 = apm.y();
                        GeoPos decodedPosition = CprDecoder.decodePosition(x0, y0, x1, y1, ODD);
                        if (decodedPosition != null) {
                            stateSetter.setPosition(decodedPosition);
                        }
                    } else if (apm.parity() == EVEN && apm.timeStampNs() - lastOddMessage.timeStampNs() <= PRECISION) {
                        double x0 = apm.x();
                        double y0 = apm.y();
                        double x1 = lastOddMessage.x();
                        double y1 = lastOddMessage.y();
                        GeoPos decodedPosition = CprDecoder.decodePosition(x0, y0, x1, y1, EVEN);
                        if (decodedPosition != null) {
                            stateSetter.setPosition(decodedPosition);
                        }
                    }
                }
            }
            case AirborneVelocityMessage avm -> {
                stateSetter.setVelocity(avm.speed());
                stateSetter.setTrackOrHeading(avm.trackOrHeading());
            }
            default -> throw new Error("Unexpected value: " + message);
        }
    }
}
