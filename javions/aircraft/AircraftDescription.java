package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents an aircraft's description which gives
 * the type of the aircraft, the number of motors that contains and the type of propulsion
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record AircraftDescription(String string) {

    private final static Pattern REGEX_DESCRIPTION = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * Checks if the string is an Aircraft's description according to regular expression or not (the string can be empty)
     *
     * @param string to be checked
     * @throws IllegalArgumentException if the string is not a description of the aircraft
     */

    public AircraftDescription {
        Preconditions.checkArgument(REGEX_DESCRIPTION.matcher(string).matches() || string.isEmpty());
    }

}


