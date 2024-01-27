package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents an object that transforms bytes coming from a AirSpy into signed 12-bits samples
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class SamplesDecoder {
    private final byte[] bytes;
    private final InputStream stream;
    private final int batchSize;
    private final static int BIAS = (int) Math.scalb(1, 11);

    /**
     * Constructs a SamplesDecoder and  returns decoded samples by using the given input to obtain bytes from AirSpy radio
     * and produces the samples in given batch size
     *
     * @param stream    input stream to obtain bytes from AirSpy radio
     * @param batchSize size of batch that will be used
     * @throws IllegalArgumentException if batch size is negative
     * @throws NullPointerException     if the input stream is null
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        bytes = new byte[2 * batchSize];
    }

    /**
     * Reads the number of bytes corresponding to a batch
     * and converts these bytes into signed samples who will be stocked in the array
     *
     * @param batch an array type short
     * @return number of converted samples divided by 2 which is always equals to batch size
     * @throws IOException              in case of input/output error
     * @throws IllegalArgumentException if length of the batch array is not equal to batch's size
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument((batch.length == batchSize));
        int readNSamples = stream.readNBytes(bytes, 0, bytes.length);
        for (int i = 0; i < readNSamples; i = 2 + i) {
            int firstByte = Byte.toUnsignedInt(bytes[i]);
            int secondByte = bytes[i + 1];
            int sample = (secondByte << Byte.SIZE | firstByte);
            batch[i / 2] = (short) (sample - BIAS);
        }
        return readNSamples / Short.BYTES;
    }
}