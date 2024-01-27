package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * Represents a decoder of a CPR position
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class CprDecoder {
    public final static double LATITUDE_ZONES_EVEN = 60;
    public final static double LATITUDE_ZONES_ODD = 59;
    public final static double WIDTH_ZONES_EVEN = 1 / LATITUDE_ZONES_EVEN;
    public final static double WIDTH_ZONES_ODD = 1 / LATITUDE_ZONES_ODD;

    private CprDecoder() {
    }

    /**
     * Returns a GeoPos with the latitude and the longitude decoded in T32
     *
     * @param x0         even longitude compacted
     * @param y0         even latitude compacted
     * @param x1         odd longitude compacted
     * @param y1         odd longitude compacted
     * @param mostRecent parity of the last position (must be 1 or 0 otherwise throws an IllegalArgumentException)
     * @return a GeoPos with the decoded latitude and longitude in T32 or returns null
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 1 || mostRecent == 0);
        if (Double.isNaN(bigZLambda0(y0, y1))) {
            return null;
        }
        double longitudeAngle, latitudeAngle, longitudeT32, latitudeT32;
        if (mostRecent == 0) {
            longitudeAngle = lambda0(x0, x1, y0, y1);
            latitudeAngle = phi0(y0, y1);
        } else {
            longitudeAngle = lambda1(x0, x1, y0, y1);
            latitudeAngle = phi1(y0, y1);
        }
        longitudeT32 = Math.rint(Units.convert(longitudeAngle, Units.Angle.TURN, Units.Angle.T32));
        latitudeT32 = Math.rint(Units.convert(latitudeAngle, Units.Angle.TURN, Units.Angle.T32));
        return GeoPos.isValidLatitudeT32((int) latitudeT32) ? new GeoPos((int) longitudeT32, (int) latitudeT32) : null;
    }

    private static double littleZPhi(double y0, double y1) {
        return Math.rint(y0 * LATITUDE_ZONES_ODD - y1 * LATITUDE_ZONES_EVEN);
    }

    private static double littleZPhi0(double y0, double y1) {
        return littleZPhi(y0, y1) < 0 ? littleZPhi(y0, y1) + LATITUDE_ZONES_EVEN : littleZPhi(y0, y1);
    }

    private static double littleZPhi1(double y0, double y1) {
        return littleZPhi(y0, y1) < 0 ? littleZPhi(y0, y1) + LATITUDE_ZONES_ODD : littleZPhi(y0, y1);
    }

    private static double phi0(double y0, double y1) {
        double angle = WIDTH_ZONES_EVEN * (littleZPhi0(y0, y1) + y0);
        return angleAdjustment(angle);
    }

    private static double phi1(double y0, double y1) {
        double angle = WIDTH_ZONES_ODD * (littleZPhi1(y0, y1) + y1);
        return angleAdjustment(angle);
    }

    private static double bigZLambda0(double y0, double y1) {

        double angleA = phi0(y0, y1);
        double angleB = phi1(y0, y1);

        angleA = Units.convert(angleA, Units.Angle.TURN, Units.Angle.RADIAN);
        angleB = Units.convert(angleB, Units.Angle.TURN, Units.Angle.RADIAN);

        double A = Math.acos(1 - ((1 - Math.cos(2 * Math.PI * WIDTH_ZONES_EVEN)) / Math.pow(Math.cos(angleA), 2)));
        double B = Math.acos(1 - ((1 - Math.cos(2 * Math.PI * WIDTH_ZONES_EVEN)) / Math.pow(Math.cos(angleB), 2)));
        A = Math.floor(Math.PI * 2 / A);
        B = Math.floor(Math.PI * 2 / B);

        if (Double.isNaN(A)) {
            A = 1;
        }
        if (Double.isNaN(B)) {
            B = 1;
        }

        if (A != B ){
            return Double.NaN;
        }

        return A;
    }


    private static double bigZLambda1(double y0, double y1) {
        return bigZLambda0(y0, y1) - 1;
    }

    private static double littleZLambda(double x0, double x1, double y0, double y1) {
        return Math.rint(x0 * bigZLambda1(y0, y1) - x1 * bigZLambda0(y0, y1));
    }

    private static double littleZlambda0(double x0, double x1, double y0, double y1) {
        return littleZLambda(x0, x1, y0, y1) < 0
                ? littleZLambda(x0, x1, y0, y1) + bigZLambda0(y0, y1)
                : littleZLambda(x0, x1, y0, y1);
    }

    private static double littleZlambda1(double x0, double x1, double y0, double y1) {
        return littleZLambda(x0, x1, y0, y1) < 0
                ? littleZLambda(x0, x1, y0, y1) + bigZLambda1(y0, y1)
                : littleZLambda(x0, x1, y0, y1);
    }

    private static double lambda0(double x0, double x1, double y0, double y1) {
        if (bigZLambda0(y0, y1) == 1) {
            return angleAdjustment(x0);
        } else {
            return angleAdjustment((1 / bigZLambda0(y0, y1)) * (littleZlambda0(x0, x1, y0, y1) + x0));
        }
    }

    private static double lambda1(double x0, double x1, double y0, double y1) {
        if (bigZLambda0(y0, y1) == 1) {
            return angleAdjustment(x1);
        } else {
            return angleAdjustment((1 / bigZLambda1(y0, y1)) * (littleZlambda1(x0, x1, y0, y1) + x1));
        }
    }

    private static double angleAdjustment(double angle) {
        return angle >= 0.5 ? angle - 1 : angle;
    }
}