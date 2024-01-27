package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents a registration number of an aircraft
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record AircraftRegistration(String string) {

    private final static Pattern REGEX_AIRCRAFT_REGISTRATION = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * Checks if the string is an Aircraft's registration number or not according to regular expression
     *
     * @param string to be checked
     * @throws IllegalArgumentException if the string does not represent registration number
     *                                  for an aircraft or if it is an empty string
     */
    public AircraftRegistration {
        Preconditions.checkArgument(REGEX_AIRCRAFT_REGISTRATION.matcher(string).matches());
        Preconditions.checkArgument(!(string.isEmpty()));
    }
}
