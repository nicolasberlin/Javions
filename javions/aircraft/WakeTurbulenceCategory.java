package ch.epfl.javions.aircraft;


/**
 * Represents the intensity of a turbulence
 * contains four category : light, medium, heavy and unknown
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM, HEAVY, UNKNOWN;

    /**
     * Converts letters into enum types to represent a type of turbulence
     *
     * @param string given to be checked
     * @return the type of turbulence or UNKNOWN if the string is not an "L" , "M" or "H"
     */

    public static WakeTurbulenceCategory of(String string) {
        return (switch (string) {
            case "L" -> WakeTurbulenceCategory.LIGHT;
            case "M" -> WakeTurbulenceCategory.MEDIUM;
            case "H" -> WakeTurbulenceCategory.HEAVY;
            default -> WakeTurbulenceCategory.UNKNOWN;
        });
    }
}
