package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;
import java.util.HexFormat;

/**
 * Represents a raw message ADS-B who consists of fourteen bytes
 *
 * @param timeStampNs of messages
 * @param bytes       of the message
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {

    /**
     * Attribute that represents the length of a message
     */
    public final static int LENGTH = 14;
    private final static byte DF = 0x11;
    private final static Crc24 CRC24 = new Crc24(Crc24.GENERATOR);
    private final static int BEGINNING_INDEX_TYPE_CODE = 51;
    private final static int SIZE_TYPE_CODE = 5;
    private final static int SIZE_ICAO_ADDRESS = 6;
    private final static int STARTING_INDEX_DF = 3;
    private final static int SIZE_DF = 5;
    private final static int START_INDEX_ICAO = 1;
    private final static int END_INDEX_ICAO = 4;
    private final static int STARTING_INDEX_ME = 4;
    private final static int ENDING_INDEX_ME = 11;
    private final static int START_INDEX_MESSAGE = 3;

    /**
     * Constructor that checks if timestamps is non negatif or
     * size of messages is equal to the length in bytes of the message
     * @throws IllegalArgumentException if timestamps is negative and
     * size of message is not equal to length in bytes of the message
     */
    public RawMessage {
        Preconditions.checkArgument(timeStampNs >= 0 );
        Preconditions.checkArgument(bytes.size() == LENGTH);
    }

    /**
     * Returns the raw message ADS-B if CRC24 is zero otherwise it returns null
     * @param timeStampNs of message
     * @param bytes of message
     * @return the raw message ADS-B or null
     */
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        if (CRC24.crc(bytes) != 0) {
            return null;
        }
        return new RawMessage(timeStampNs, new ByteString(bytes));
    }

    /**
     * Returns length of message if the five first bit or five most significant bits is equal to downlink format
     * otherwise it is not a message, it returns zero
     * @param byte0 first byte of message
     * @return length of the message if five first bit of byte0 is equal to DF
     */

    public static int size(byte byte0) {
        int firstFiveBits = Bits.extractUInt(byte0, START_INDEX_MESSAGE, SIZE_DF);
        if (firstFiveBits == DF) {
            return LENGTH;
        }
        return 0;
    }

    /**
     * Returns the type code of the message who is situated five most significant bits of the payload
     * @param payload ME of message
     * @return type code of ME
     */
    public static int typeCode(long payload) {
        return Bits.extractUInt(payload, BEGINNING_INDEX_TYPE_CODE, SIZE_TYPE_CODE);
    }
    /**
     * Downlink format of message is the link between aircraft and ground
     * @return DF of message who is situated in five most significant bit in message
     */
    public int downLinkFormat() {
        int firstByte = bytes.byteAt(0);
        return Bits.extractUInt(firstByte, STARTING_INDEX_DF, SIZE_DF);
    }

    /**
     * Returns an ICAO address
     * @return ICAO address of message
     */
    public IcaoAddress icaoAddress() {
        HexFormat hex = HexFormat.of().withUpperCase();
        long icaoAddress = bytes.bytesInRange(START_INDEX_ICAO, END_INDEX_ICAO);
        return new IcaoAddress(hex.toHexDigits(icaoAddress, SIZE_ICAO_ADDRESS));
    }

    /**
     * Payload is content of the message
     * @return payload of the message
     */
    public long payload(){
        return bytes.bytesInRange(STARTING_INDEX_ME, ENDING_INDEX_ME);
    }

    /**
     * Type code allows us to know what payload of the message contains
     * @return type code of payload
     */
    public int typeCode() {
        long message = payload();
        return typeCode(message);
    }
}
