package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Represents a velocity message that has 4 subtypes of message
 * according to the air speed and the ground speed
 *
 * @param timeStampNs    timestamp of the message, in nanoseconds
 * @param icaoAddress    address ICAO of aircraft
 * @param speed          an aircraft speed, in meter per second (m/s)
 * @param trackOrHeading the direction of moving of an aircraft in radians
 *                       track is the direction in which the shadow of the aircraft moves on the ground
 *                       heading is the direction in which the nose of the aircraft is pointing
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed,
                                      double trackOrHeading) implements Message {

    private static final int LENGTH_PAYLOAD = 56;
    private final static int STARTING_INDEX_SUBTYPE = LENGTH_PAYLOAD - Byte.SIZE;
    private final static int SIZE_SUBTYPE = 3;
    private final static int STARTING_INDEX_TWENTY_TWO_BITS = 21;
    private final static int SIZE_TWENTY_TWO_BITS = 22;
    private final static int STARTING_INDEX_VEW_HDG = 11;
    private final static int SIZE_VEW_VNS_HDG_AS = 10;
    private final static int STARTING_INDEX_DEW_SH = 21;
    private final static int SIZE_DEW_DNS_SH = 1;
    private final static int STARTING_INDEX_DNS = 10;
    private final static int STARTING_INDEX_VNS_AS = 0;

    /**
     * Checks if the ICAO address is null or not and checks if timestamps, speed and
     * trackOrHeading are non-negative
     *
     * @throws NullPointerException     if the ICAO address is null
     * @throws IllegalArgumentException if the attributes timestampNs, speed and trackOrHeading are negative
     */
    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(speed >= 0);
        Preconditions.checkArgument(trackOrHeading >= 0);
    }

    /**
     * Returns an airborne velocity message corresponding to the raw message
     * otherwise it returns null if the subtype of message is not 1,2,3,4
     * or if the velocity or the direction cannot be determined
     *
     * @param rawMessage to be decoded to find the velocity and the direction
     * @return an airborne velocity message
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        long timeStamps = rawMessage.timeStampNs();
        long payload = rawMessage.payload();
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        int subType = Bits.extractUInt(payload, STARTING_INDEX_SUBTYPE, SIZE_SUBTYPE);
        long twentyTwoBits = Bits.extractUInt(payload, STARTING_INDEX_TWENTY_TWO_BITS, SIZE_TWENTY_TWO_BITS);

        double speedNorm = calculateSpeedNorm(twentyTwoBits, subType);
        double trackOrHeading = getTrackOrHeading(twentyTwoBits, subType);

        if (!Double.isNaN(speedNorm) && !Double.isNaN(trackOrHeading)) {
            return new AirborneVelocityMessage(timeStamps, icaoAddress, speedNorm, trackOrHeading);
        }

        return null;
    }

    private static double calculateSpeedNorm(long value, int type) {
        double multiple = 1;
        if (type == 1 || type == 2) {
            double groundSpeed = getGroundSpeedNorm(value);
            if (Double.isNaN(groundSpeed)) {
                return Double.NaN;
            }
            if (type == 2) {
                multiple = 4;
            }
            return multiple * groundSpeed;
        } else if (type == 3 || type == 4) {
            double airSpeedNorm = getAirSpeedNorm(value);
            if (Double.isNaN(airSpeedNorm)) {
                return Double.NaN;
            }
            if (type == 4) {
                multiple = 4;
            }
            return multiple * airSpeedNorm;
        }
        return Double.NaN;
    }

    private static double getGroundSpeedNorm(long value) {
        int vns = Bits.extractUInt(value, STARTING_INDEX_VNS_AS, SIZE_VEW_VNS_HDG_AS);
        int vew = Bits.extractUInt(value, STARTING_INDEX_VEW_HDG, SIZE_VEW_VNS_HDG_AS);
        if (vns == 0 || vew == 0) {
            return Double.NaN;
        }
        double velocityNorm = Math.hypot(vns - 1, vew - 1);
        return Units.convertFrom(velocityNorm, Units.Speed.KNOT);
    }

    private static double getAirSpeedNorm(long value) {
        double as = Bits.extractUInt(value, STARTING_INDEX_VNS_AS, SIZE_VEW_VNS_HDG_AS);
        return as == 0 ? Double.NaN : Units.convertFrom(as - 1, Units.Speed.KNOT);
    }

    private static double calculateHeading(long value) {
        int sh = Bits.extractUInt(value, STARTING_INDEX_DEW_SH, SIZE_DEW_DNS_SH);
        if (sh == 1) {
            int hdg = Bits.extractUInt(value, STARTING_INDEX_VEW_HDG, SIZE_VEW_VNS_HDG_AS);
            double calculatedHdg = Math.scalb(hdg, -10);
            return Units.convertFrom(calculatedHdg, Units.Angle.TURN);
        }
        return Double.NaN;
    }

    private static double getTrackOrHeading(long value, int type) {
        double angle;
        if (type == 1 || type == 2) {
            double eastWest = Bits.extractUInt(value, STARTING_INDEX_DEW_SH, SIZE_DEW_DNS_SH);
            double northSouth = Bits.extractUInt(value, STARTING_INDEX_DNS, SIZE_DEW_DNS_SH);
            angle = calculateTrack(value, northSouth, eastWest);
        } else {
            angle = calculateHeading(value);
            if (Double.isNaN(angle)) {
                return Double.NaN;
            }
        }
        return angle;
    }

    private static double calculateTrack(long value, double directionNS, double directionEW) {
        int vns = Bits.extractUInt(value, STARTING_INDEX_VNS_AS, SIZE_VEW_VNS_HDG_AS) - 1;
        int vew = Bits.extractUInt(value, STARTING_INDEX_VEW_HDG, SIZE_VEW_VNS_HDG_AS) - 1;
        double angle = Math.atan2(Math.abs(vns), Math.abs(vew));
        if (directionEW == 1 && directionNS == 1) {
            angle = Math.PI * 1.5 - angle;
        } else if (directionEW == 1 && directionNS == 0) {
            angle = Math.PI * 1.5 + angle;
        } else if (directionEW == 0 && directionNS == 1) {
            angle = Math.PI * 0.5 + angle;
        } else {
            angle = Math.PI * 0.5 - angle;
        }
        return angle;
    }
}
