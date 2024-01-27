package ch.epfl.javions;

/**
 * Represents a cyclic redundancy check of 24 bits which verify the validity of the received messages
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class Crc24 {

    /**
     * GENERATOR : used to calculate ABS-S FFF409 messages
     */
    public final static int GENERATOR = 0XFFF409;
    private final static int CRC_LENGTH = 24;
    private final static int MASK = 0xFFFFFF;
    private final static int GENERATOR_SIZE = 256;
    private final static int BIT_SIZE = 1;
    private final int[] array;


    /**
     * Constructs the Crc 24 computer with the generator
     *
     * @param generator an argument
     */
    public Crc24(int generator) {
        this.array = buildTable(generator);
    }

    /**
     * Returns the CRC of bytes
     *
     * @param bytes an array of the message
     * @return the crc
     */
    public int crc(byte[] bytes) {
        int crc = 0;
        for (int i = 0; i < bytes.length; i++) {
            crc = ((crc << Byte.SIZE) | Byte.toUnsignedInt(bytes[i])) ^ array[Bits.extractUInt(crc, CRC_LENGTH - Byte.SIZE, Byte.SIZE)];
        }
        for (int j = 0; j < CRC_LENGTH / Byte.SIZE; j++) {
            crc = ((crc << 8)) ^ array[Bits.extractUInt(crc, CRC_LENGTH - Byte.SIZE, Byte.SIZE)];
        }
        return MASK & crc;
    }

    /**
     * Return the CRC of an array of bytes given the generator
     *
     * @param generator of the CRC
     * @param array     table of bytes contains messages
     * @return crc
     */
    private static int crc_bitwise(int generator, byte[] array) {
        int[] table = new int[]{0, generator};
        int crc = 0;
        for (int i = 0; i < array.length; i++) {
            for (int bit = Byte.SIZE - 1; bit >= 0; bit--) {
                int index = Bits.extractUInt(crc, CRC_LENGTH - 1, BIT_SIZE);
                crc = ((crc << 1) | Bits.extractUInt(array[i], bit, BIT_SIZE)) ^ table[index];
            }
        }
        for (int j = 0; j < CRC_LENGTH; j++) {
            crc = ((crc << 1)) ^ table[Bits.extractUInt(crc, CRC_LENGTH - 1, BIT_SIZE)];
        }
        return MASK & crc;
    }


    /**
     * Creates a table of length 256 that contains each element corresponds a generator
     *
     * @param generator of the table
     * @return the table
     */
    private static int[] buildTable(int generator) {
        int[] table = new int[GENERATOR_SIZE];
        for (int i = 0; i < table.length; i++) {
            table[i] = crc_bitwise(generator, new byte[]{(byte) i});
        }
        return table;
    }
}
