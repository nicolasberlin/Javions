package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Represents a sequence of bytes interpreted as unsigned
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class ByteString {
    private final byte[] bytes;

    /**
     * Constructs a sequence of bytes
     *
     * @param bytes an array of bytes
     */
    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    /**
     * Converts a hexadecimal string to a byte string
     *
     * @param string of hexadecimals
     * @return ByteString
     * @throws IllegalArgumentException if the size of the string is odd or
     *                                  if it contains a character that is not a hexadecimal character
     */
    public static ByteString ofHexadecimalString(String string) {
        HexFormat hexFormat = HexFormat.of().withUpperCase();
        return new ByteString(hexFormat.parseHex(string));
    }

    /**
     * Returns size of the bytes
     *
     * @return number of bytes that the sequence contains
     */
    public int size() {
        return bytes.length;
    }

    /**
     * Returns the unsigned byte at the given index
     *
     * @param index of the byte
     * @return unsigned value of the byte at the index
     * @throws IndexOutOfBoundsException if the index is not in the range of the array size
     */
    public int byteAt(int index) {
        Objects.checkIndex(index, size());
        return Byte.toUnsignedInt(bytes[index]);
    }

    /**
     * Returns a sublist of bytes
     *
     * @param fromIndex index
     * @param toIndex   index of the end of the array
     * @return a long value of bytes between fromIndex(inclusive) and toIndex (exclusive)
     * @throws IndexOutOfBoundsException if fromIndex and toIndex are not
     *                                   between 0 (inclusive) and the size of the sequence (exclusive)
     * @throws IllegalArgumentException  if the difference between toIndex and fromIndex
     *                                   is not less than the number of bytes contained in a long value
     */
    public long bytesInRange(int fromIndex, int toIndex) {

        Preconditions.checkArgument(toIndex - fromIndex < Long.BYTES);
        Objects.checkFromToIndex(fromIndex, toIndex, size());
        long bytes = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            bytes = bytes << Byte.SIZE;
            bytes = byteAt(i) | bytes;
        }
        return bytes;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ByteString a) && Arrays.equals(this.bytes, a.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);

    }

    @Override
    public String toString() {
        return HexFormat.of().withUpperCase().formatHex(this.bytes);
    }
}
