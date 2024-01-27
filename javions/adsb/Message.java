package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
/**
 * Provides to methods to be overridden that give information about a message
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public interface Message {

    /**
     * Returns ths timestamps of the message in nanoseconds
     *
     * @return timestamps of the message
     */
    long timeStampNs();

    /**
     * Returns the ICAO address of message
     *
     * @return the ICAO address
     */
    IcaoAddress icaoAddress();
}
