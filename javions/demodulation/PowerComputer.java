package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a PowerComputer, an object capable of calculating power samples
 * with sign samples received from a SampleDecoder
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class PowerComputer {
    private final short[] arrayOfSamples;
    private final short[] lastEightSamples = new short[Byte.SIZE];
    private final SamplesDecoder samplesDecoder;

    /**
     * Constructs a PowerComputer with a stream and samples decoded from SamplesDecoder
     *
     * @param stream    an input stream of bytes
     * @param batchSize size of batch that contains the samples
     * @throws IllegalArgumentException if batchSize is greater than 0 or if batchSize is not a multiple of eight
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize % Byte.SIZE == 0);
        Preconditions.checkArgument(batchSize > 0);
        arrayOfSamples = new short[2 * batchSize];
        samplesDecoder = new SamplesDecoder(stream, 2 * batchSize);
    }

    /**
     * Reads the number of samples required in a batch of power samples and store them in the batch given as argument.
     * It returns the number of power samples put in the array
     *
     * @param batch array that stores power samples
     * @return number of power samples placed in the batch
     * @throws IOException in case of input/output error
     */
    public int readBatch(int[] batch) throws IOException {
        int count = samplesDecoder.readBatch(arrayOfSamples);
        for (int i = 0; i < arrayOfSamples.length; i++) {
            lastEightSamples[i % Byte.SIZE] = arrayOfSamples[i];
            if (i % 2 == 1) {
                double oddSamples = Math.pow((lastEightSamples[1] - lastEightSamples[3]
                        + lastEightSamples[5] - lastEightSamples[7]), 2);
                double evenSamples = Math.pow((lastEightSamples[0] - lastEightSamples[2]
                        + lastEightSamples[4] - lastEightSamples[6]), 2);
                batch[i / 2] = (int) (oddSamples + evenSamples);
            }
        }
        return count / 2;
    }
}
