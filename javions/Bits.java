package ch.epfl.javions;

import java.util.Objects;

/**
 * Contains methods to extract subsets of a value of 64 bits in type long
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class Bits {

    private Bits() {
    }

    /**
     * Extracts bits of the given value from start (inclusive) to start + size (exclusive)
     *
     * @param value to be extracted
     * @param start initial index of range
     * @param size  of range
     * @return unsigned extracted value in length "size"
     * @throws IllegalArgumentException  if size is not greater than 0 (exclusive) and less than 32 (exclusive)
     * @throws IndexOutOfBoundsException if the range defined by start and start + size is not between 0 (included) and 64 (excluded)
     */

    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument((size > 0 && size < Integer.SIZE));
        int index = Objects.checkFromIndexSize(start, size, Long.SIZE);
        int mask = (1 << size) - 1;
        int newValue = (int) (value >>> index);
        return newValue & mask;
    }

    /**
     * Checks if the bit is equals to 1
     *
     * @param value to be checked
     * @param index of the bit
     * @return true if the bit at the given index is equals to 1, otherwise false
     * @throws IndexOutOfBoundsException if the value is not between 0 (included) and 64 (excluded)
     */
    public static boolean testBit(long value, int index) {
        int newValue = (int) (value >>> Objects.checkIndex(index, Long.SIZE));
        int bit = (newValue & 1);
        return bit == 1;
    }
}
