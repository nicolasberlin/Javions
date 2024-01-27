package ch.epfl.javions;

/**
 * Contains methods to project graphic coordinates
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class WebMercator {
    private final static int START_PIXEL_POWER = 8;

    private WebMercator() {
    }

    /**
     * Returns x coordinates projected on the map according to zoom level
     *
     * @param zoomLevel of the map
     * @param longitude given to x coordinates
     * @return x coordinates according to longitude in radians at zoom level
     */
    public static double x(int zoomLevel, double longitude) {
        double longitudeInRadians = Units.convertTo(longitude, Units.Angle.TURN);
        return Math.scalb(longitudeInRadians + 0.5, START_PIXEL_POWER + zoomLevel);
    }

    /**
     * Returns y coordinates projected on the map according to zoom level
     *
     * @param zoomLevel of the map
     * @param latitude  given to y coordinates
     * @return y coordinates according to latitude in radians at zoom level
     */
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(Units.convertTo(-(Math2.asinh(Math.tan(latitude))), Units.Angle.TURN) + 0.5, (START_PIXEL_POWER + zoomLevel));
    }
}
