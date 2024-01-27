package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents a type designator for the aircraft types
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record AircraftTypeDesignator(String string) {
    private final static Pattern REGEX_DESIGNATOR = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * Checks if the string is an aircraft's type designator according to regular expression and can be empty
     *
     * @param string to be checked
     * @throws IllegalArgumentException if the string is not a type designator of the aircraft
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(REGEX_DESIGNATOR.matcher(string).matches() || string.isEmpty());
    }
}
