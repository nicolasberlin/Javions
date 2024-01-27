package ch.epfl.javions;

/**
 * Contains mathematical static methods
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class Math2 {

    private Math2() {
    }

    /**
     * Limits the value of v in the interval of min and max and returns min if v is less than min,
     * max if v is greater than max, otherwise v
     *
     * @param min lower bound of the interval
     * @param v   an integer value
     * @param max upper bound of the interval
     * @return max if v greater than max, min if v lower than min, otherwise v
     * @throws IllegalArgumentException if min greater than max
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <=  max);
        return (v > max) ? max : (Math.max(v, min));
    }

    /**
     * Calculates the inverse hyperbolic sinus
     *
     * @param x a double value
     * @return the inverse hyperbolic sinus of the given value x
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + Math.pow(x, 2)));
    }
}
