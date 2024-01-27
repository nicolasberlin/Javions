package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents a window of power samples. An object that can manipulates
 * two arrays of power samples and move in the stream
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class PowerWindow {
    private final int windowSize;
    private int windowPosition;
    private final static int BATCH_SIZE = (int) Math.pow(2, 16);
    private int samplesNumber;
    private long position;
    private int[] evenSamples = new int[BATCH_SIZE];
    private int[] oddSamples = new int[BATCH_SIZE];
    private final PowerComputer powerComputer;

    /**
     * Constructs a window
     *
     * @param stream     of bytes
     * @param windowSize size of the window
     * @throws IllegalArgumentException if the window is larger than batch size, if it is null or negative
     * @throws IOException              if an input error occurs
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(BATCH_SIZE >= windowSize && windowSize > 0);
        this.windowSize = windowSize;
        windowPosition = 0;
        powerComputer = new PowerComputer(stream, BATCH_SIZE);
        samplesNumber = powerComputer.readBatch(evenSamples);
    }

    /**
     * Returns window size
     *
     * @return window size
     */
    public int size() {
        return windowSize;
    }

    /**
     * Returns window's position in the stream
     *
     * @return window's position in the stream
     */
    public long position() {
        return position;
    }

    /**
     * Advances the window by one sample
     *
     * @throws IOException when an I/O error occurs during the read of the stream
     */
    public void advance() throws IOException {
        windowPosition++;
        position++;
        samplesNumber--;
        if ((windowPosition + windowSize - 1) % BATCH_SIZE == 0) {
            samplesNumber += powerComputer.readBatch(oddSamples);
        } else if (position % BATCH_SIZE == 0) {
            int[] temporary = evenSamples;
            evenSamples = oddSamples;
            oddSamples = temporary;
            windowPosition = 0;
        }
    }

    /**
     * Advances the window by a given number of samples
     *
     * @param offset number of samples
     * @throws IOException              if an input error occurs
     * @throws IllegalArgumentException if the offset is less than or equal to zero
     */
    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset > 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }

    /**
     * Checks if the window is full
     *
     * @return true if the window is full
     */
    public boolean isFull() {
        return samplesNumber >= windowSize;
    }

    /**
     * Returns the sample at the given index
     *
     * @param i index
     * @return the sample at the specified position
     * @throws IndexOutOfBoundsException if the index is not between 0 (inclusive) and the window's size (exclusive)
     */
    public int get(int i) {
        Objects.checkIndex(i, windowSize);
        if (i + windowPosition < evenSamples.length) {
            return evenSamples[windowPosition + i];
        } else {
            return oddSamples[windowPosition + i - evenSamples.length];
        }
    }
}