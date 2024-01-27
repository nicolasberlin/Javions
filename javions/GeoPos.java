package ch.epfl.javions;

/**
 * Represents geographic coordinates in t32
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {

    /**
     * Constructs a geographic coordinate with longitude and latitude
     *
     * @param longitudeT32 longitude expressed in t32
     * @param latitudeT32  latitude expressed in t32
     * @throws IllegalArgumentException if the latitude is not in t32
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * Check if the latitude is expressed in t32
     *
     * @param latitudeT32 latitude to be checked
     * @return true if the latitude is expressed in t32, otherwise false
     */
    public static boolean isValidLatitudeT32(int latitudeT32) {
        return ((Math.pow(2, 30) >= latitudeT32) && (latitudeT32 >= -Math.pow(2, 30)));
    }

    /**
     * Converts longitudeT32 in radians
     *
     * @return longitudeT32 in radians
     */
    public double longitude() {
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**
     * Converts latitudeT32 in radians
     *
     * @return latitudeT32 in radians
     */
    public double latitude() {
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    /**
     * Shows the longitude and the latitude in degrees
     *
     * @return textual representation of longitude and latitude in degrees
     */
    @Override
    public String toString() {
        return "(" + Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°"
                + ", " + Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°)";
    }
}
