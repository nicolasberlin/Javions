package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * Allows to modify an attribute of the state of the aircraft
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public interface AircraftStateSetter {
    /**
     * Changes the timestamp of the last message received
     *
     * @param timeStampsNs the new timestamp of the message
     */
    void setLastMessageTimeStampNs(long timeStampsNs);

    /**
     * Changes the category of the aircraft
     *
     * @param category the new category of the aircraft
     */
    void setCategory(int category);

    /**
     * Changes the callsign of the aircraft
     *
     * @param callSign the new call sign of the aircraft
     */

    void setCallSign(CallSign callSign);

    /**
     * Changes the position of the aircraft
     *
     * @param position the new position of the aircraft
     */

    void setPosition(GeoPos position);

    /**
     * Changes the altitude of the aircraft
     *
     * @param altitude the new altitude of the aircraft
     */
    void setAltitude(double altitude);

    /**
     * Changes the velocity of the aircraft
     *
     * @param velocity the new velocity of the aircraft
     */
    void setVelocity(double velocity);

    /**
     * Changes the direction of the aircraft
     *
     * @param trackOrHeading new direction of the aircraft
     */
    void setTrackOrHeading(double trackOrHeading);
}
