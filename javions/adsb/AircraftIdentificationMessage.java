package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Represents an aircraft identification and category messages which allows the aircraft to communicate
 *
 * @param timeStampNs in nanoseconds
 * @param icaoAddress of the aircraft
 * @param category    of the aircraft. If it is a plane, a balloon etc...
 * @param callSign    of the aircraft
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {

    private final static int SIZE_PAYLOAD = 56;
    private static final int START_INDEX_CATEGORY = SIZE_PAYLOAD - Byte.SIZE;
    private static final int SIZE_CA = 3;
    private static final int START_FIRST_CHARACTER = 42;
    private static final int SIZE_CHARACTER = 6;
    private static final int CALL_SIGN_SIZE = 8;
    private final static String CHAR_TABLE = "*ABCDEFGHIJKLMNOPQRSTUVWXYZ***** ***************0123456789******";
    private final static int MIN_TYPE_CODE_AIM = 1;
    private final static int MAX_TYPE_CODE_AIM = 4;
    private final static char INVALID_CHARACTER = '*';
    private final static int SIZE_MESSAGE = 14;


    /**
     * Checks if the ICAO address or the call sign is null or not
     *
     * @throws NullPointerException     if ICAO address of aircraft or its call sign is null
     * @throws IllegalArgumentException if timestamps is negative
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * Returns an identification message sent by aircraft
     *
     * @param rawMessage message sent by aircraft
     * @return an identification message otherwise null if it not an identification message or
     * the call sign contains an invalid character
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        IcaoAddress icaoAddressRawMess = rawMessage.icaoAddress();
        int typeCode = rawMessage.typeCode();
        long payload = rawMessage.payload();
        long timeStamps = rawMessage.timeStampNs();
        char[] characterTable = new char[CALL_SIGN_SIZE];
        if (MIN_TYPE_CODE_AIM <= typeCode && typeCode <= MAX_TYPE_CODE_AIM) {
            int category = returnCategory(payload, typeCode);
            for (int i = 0; i < CALL_SIGN_SIZE; i++) {
                int index = Bits.extractUInt(payload, START_FIRST_CHARACTER - SIZE_CHARACTER * i, SIZE_CHARACTER);
                if (CHAR_TABLE.charAt(index) == INVALID_CHARACTER) {
                    return null;
                }
                characterTable[i] = CHAR_TABLE.charAt(index);
            }
            String callSign = convertToString(characterTable);
            return new AircraftIdentificationMessage(timeStamps, icaoAddressRawMess, category, new CallSign(callSign));
        }
        return null;
    }

    private static int returnCategory(long payload, int typeCode) {
        int partialCategory = Bits.extractUInt(payload, START_INDEX_CATEGORY, SIZE_CA);
        int nibbleMSB = (SIZE_MESSAGE - typeCode) << 4;
        return Byte.toUnsignedInt((byte) nibbleMSB) | Byte.toUnsignedInt((byte) partialCategory);
    }

    private static String convertToString(char[] chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char aChar : chars) {
            stringBuilder.append(aChar);
        }
        String string = stringBuilder.toString();
        return string.stripTrailing();
    }

}
