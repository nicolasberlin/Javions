package ch.epfl.javions;

/**
 * Contains SI prefixes, base units and methods of conversion
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class Units {

    private Units() {
    }

    /**
     * SI definitions
     */
    public final static double CENTI = 1e-2;
    public final static double KILO = 1e+3;

    /**
     * Represents different units of angle
     */
    public static class Angle {
        /**
         * Units of an angle
         */
        public final static double RADIAN = 1; //base unit
        public final static double TURN = Math.scalb(Math.PI, (int) RADIAN);
        public final static double DEGREE = TURN / 360;
        public final static double T32 = Math.scalb(TURN, -32);
    }

    /**
     * Represents different units of a length
     */
    public static class Length {
        /**
         * Units of length
         */
        public final static double METER = 1;
        public final static double CENTIMETER = CENTI * METER;
        public final static double KILOMETER = KILO * METER;
        public final static double INCH = 2.54 * CENTIMETER;
        public final static double FOOT = 12 * INCH;
        public final static double NAUTICAL_MILE = 1852 * METER;
    }

    /**
     * Represents different units of time
     */
    public static class Time {
        /**
         * Units of time
         */
        public final static double SECOND = 1; //base unit
        public final static double MINUTE = 60 * SECOND;
        public final static double HOUR = 60 * MINUTE;
    }

    /**
     * Represents different units of speed
     */
    public static class Speed {
        /**
         * Units of speed
         */
        public final static double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        public final static double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
    }

    /**
     * Converts the given value from a unit to the wanted unit
     *
     * @param value    to be converted
     * @param fromUnit starting unit
     * @param toUnit   final unit
     * @return converted value
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }

    /**
     * Converts the given value from a unit to the base unit
     *
     * @param value    to be converted
     * @param fromUnit starting unit
     * @return converted value
     */
    public static double convertFrom(double value, double fromUnit) {
        return convert(value, fromUnit, 1);
    }

    /**
     * Converts the given value from the base unit to the unit wanted
     *
     * @param value  to be converted
     * @param toUnit final unit
     * @return converted value
     */
    public static double convertTo(double value, double toUnit) {
        return convert(value, 1, toUnit);
    }
}
