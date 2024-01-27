package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.Units.Length.FOOT;
import static ch.epfl.javions.Units.Length.METER;

/**
 * Represents an ADS-B message of an airborne position which allows the aircraft to send its position during the flight
 *
 * @param timeStampNs in nanoseconds
 * @param icaoAddress the ICAO address of aircraft
 * @param altitude    at which the aircraft was at the time the message was sent, in meters
 * @param parity      of the message (if it is 0 then the message is even, if it is 1 then it is odd)
 * @param x           local and standardized longitude of the aircraft when the message was sent
 * @param y           local and standardized latitude at which the aircraft was at the time,
 *                    the message was sent
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x,
                                      double y) implements Message {
    private final static int START_INDEX_ALT = 36;
    private final static int SIZE_ALTITUDE = 12;
    private final static int ALT_BIT_INDEX = 4;
    private final static int ALT_BASE = -1000;
    private final static int MULTIPLE = 25;
    private final static int START_INDEX_FORMAT = START_INDEX_ALT - 2;
    private final static int BASE_ALT = -1300;
    private final static int MULTIPLE_LSB = 100;
    private final static int MULTIPLE_MSB = 500;
    private final static int ODD = 1;
    private final static int START_INDEX_LAT_CPR = 17;
    private final static int START_INDEX_LON_CPR = 0;
    private final static int SIZE_LAT_LON_CPR = 17;
    private final static int START_INDEX_MSB = 3;
    private final static int BIT_SIZE = 1;
    private final static List<Integer> INVALID_VALUES = List.of(0, 5, 6);
    private final static int LOWER_MIN_TYPE_CODE_APM = 9;
    private final static int LOWER_MAX_TYPE_CODE_APM = 18;
    private final static int HIGHER_MIN_TYPE_CODE_APM = 20;
    private final static int HIGHER_MAX_TYPE_CODE_APM = 22;
    private final static double NORMALIZING_COORDINATES = Math.scalb(1, -17);
    private final static int SHIFT_TO_LEFT_NIBBLE = 4;
    private final static int INDEX_Q = 4;


    /**
     * Checks if the parameters are correct or not
     *
     * @throws NullPointerException     if ICAO address is null
     * @throws IllegalArgumentException if timestampNs is negative or the parity is not 0 or 1 or x and y are not in the
     *                                  interval 0 (inclusive) and 1 (exclusive)
     */
    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument((parity == 0) || (parity == 1));
        Preconditions.checkArgument(x >= 0 && x < 1);
        Preconditions.checkArgument(y >= 0 && y < 1);
    }

    /**
     * Returns an airborne position message which is sent by aircraft
     *
     * @param rawMessage sent by aircraft
     * @return message of airborne position
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        int typeCode = rawMessage.typeCode();
        long payload = rawMessage.payload();
        long timeStamps = rawMessage.timeStampNs();
        long altitude = extractUInt(payload, START_INDEX_ALT, SIZE_ALTITUDE);
        int format = extractUInt(payload, START_INDEX_FORMAT, BIT_SIZE);

        if ((LOWER_MIN_TYPE_CODE_APM <= typeCode && typeCode <= LOWER_MAX_TYPE_CODE_APM)
                || (HIGHER_MIN_TYPE_CODE_APM <= typeCode && typeCode <= HIGHER_MAX_TYPE_CODE_APM)) {
            double calculatedAltitude = altitudeCalculator(altitude);
            if (!Double.isNaN(calculatedAltitude)) {
                return new AirbornePositionMessage(timeStamps, icaoAddress, calculatedAltitude,
                        format, getLongitude_Cpr(payload), getLatitude_Cpr(payload));
            }
        }
        return null;
    }

    private static double getLatitude_Cpr(long payload) {
        int lat_Cpr = extractUInt(payload, START_INDEX_LAT_CPR, SIZE_LAT_LON_CPR);
        return lat_Cpr * NORMALIZING_COORDINATES;
    }

    private static double getLongitude_Cpr(long payload) {
        int lon_Cpr = extractUInt(payload, START_INDEX_LON_CPR, SIZE_LAT_LON_CPR);
        return lon_Cpr * NORMALIZING_COORDINATES;
    }

    /**
     * Disentangle the twelve bits in altitude and returns new value of the altitude
     *
     * @param altitude its bits to be disentangled
     * @return a new altitude whose bits are disentangled
     */
    private static long disentangling(long altitude) {
        int result = 0;
        int[] index = {4, 10, 5, 11};
        for (int k : index) {
            for (int j = 0; j <= index.length; j += 2) {
                int temp = (int) ((altitude >> (k - j)) & 1);
                result = (result << 1) | temp;
            }
        }
        return result;
    }

    private static int decoderGrayCode(int value, int index) {
        int decodedAltitude = 0;
        for (int i = 0; i < index; i++) {
            decodedAltitude = ((value >> i) ^ decodedAltitude);
        }
        return decodedAltitude;
    }

    /**
     * According to parity of most significant bit, recalculate the altitude reflected or nonreflected version
     *
     * @param parity   of the most significant bit
     * @param valueLSB three least significant bit of the decoded altitude
     * @param valueMSB nine most significant bits of the decoded altitude
     * @return reflected or non reflected version of the altitude
     */

    private static double reflectedNonReflectedAltitude(int parity, long valueLSB, long valueMSB) {
        if (parity == ODD) {
            long reflectedLSB = 6 - valueLSB;
            double reflectedValue = BASE_ALT + reflectedLSB * MULTIPLE_LSB + valueMSB * MULTIPLE_MSB;
            return Units.convert(reflectedValue, FOOT, METER);
        }
        double altitude = BASE_ALT + valueLSB * MULTIPLE_LSB + valueMSB * MULTIPLE_MSB;
        return Units.convert(altitude, FOOT, METER);

    }

    private static boolean verifyInvalidValues(int value) {
        return INVALID_VALUES.contains(value);
    }

    private static double altitudeCalculator(long altitude) {
        if (Bits.testBit(altitude, ALT_BIT_INDEX)) {
            int leftSideOfQ = extractUInt(altitude, INDEX_Q + 1, SIZE_ALTITUDE + INDEX_Q + 1);
            int rightSideOfQ = extractUInt(altitude, 0, INDEX_Q);
            int shifted = leftSideOfQ << SHIFT_TO_LEFT_NIBBLE;
            int newAltitude = shifted | rightSideOfQ;
            double altitudeInFeet = ALT_BASE + newAltitude * MULTIPLE;
            return Units.convert(altitudeInFeet, FOOT, METER);
        } else {
            long disentangledAltitude = disentangling(altitude);
            int threeLeastSignificantBits = extractUInt(disentangledAltitude, 0, SIZE_ALTITUDE - 9);
            int nineMostSignificantBits = extractUInt(disentangledAltitude, START_INDEX_MSB, SIZE_ALTITUDE - 3);
            int decodedLSB = decoderGrayCode(threeLeastSignificantBits, SIZE_ALTITUDE - 9);
            int decodedMSB = decoderGrayCode(nineMostSignificantBits, SIZE_ALTITUDE - 3);
            int parityOfDecodedMSB = extractUInt(decodedMSB, 0, BIT_SIZE);
            if (!verifyInvalidValues(decodedLSB)) {
                if (decodedLSB == 7) {
                    decodedLSB = 5;
                }
                return reflectedNonReflectedAltitude(parityOfDecodedMSB, decodedLSB, decodedMSB);
            }
            return Double.NaN;
        }
    }
}