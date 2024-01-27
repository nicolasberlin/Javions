package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents an ICAO Address of aircraft
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record IcaoAddress(String string) {
    private final static Pattern REGEX_ICAO_ADDRESS = Pattern.compile("[0-9A-F¨]{6}");

    /**
     * Checks if the string is an ICAO address according to the regular expression
     *
     * @param string to be checked
     * @throws IllegalArgumentException if the string does not represent an ICAO address or if it is an empty string
     */
    public IcaoAddress {
        Preconditions.checkArgument(REGEX_ICAO_ADDRESS.matcher(string).matches());
        Preconditions.checkArgument(!(string.isEmpty()));
    }
}
