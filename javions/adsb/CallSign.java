package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents a call sign of an aircraft
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record CallSign(String string) {

    private final static Pattern REGEX_CALL_SIGN = Pattern.compile("[A-Z0-9]{0,8}");

    /**
     * Checks if the string is a call sign or not (the string can be empty)
     *
     * @param string to be checked
     * @throws IllegalArgumentException if the string is not a call sign
     */
    public CallSign {
        Preconditions.checkArgument(REGEX_CALL_SIGN.matcher(string).matches() || string.isEmpty());
    }
}
