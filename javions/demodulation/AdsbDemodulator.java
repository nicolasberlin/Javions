package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;


/**
 * Represents a demodulator of ADS-B messages
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class AdsbDemodulator {
    private final PowerWindow powerWindow;
    private final static int WINDOW_SIZE = 1200;
    private final static int MESSAGE_LENGTH = 14;
    private final static int PREAMBLE_OF_SAMPLES = 80;
    private final static int CONVERSION_TO_NS = 100;
    private final static int FIRST_BYTE = 0;

    /**
     * Constructs a demodulator getting the samples of the input stream
     *
     * @param samplesStream samples of the stream
     * @throws IOException if an I/O error occurs
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        powerWindow = new PowerWindow(samplesStream, WINDOW_SIZE);
    }

    /**
     * Returns the next ADS-B message
     *
     * @return a RawMessage or null if there is no message
     * @throws IOException an exception that is thrown when an I/O error occurs during the read of the stream
     */
    public RawMessage nextMessage() throws IOException {
        byte[] bytes = new byte[MESSAGE_LENGTH];
        int oldSumOfPeaks = 0;
        while (powerWindow.isFull()) {
            int sumOfPeaks = getSumOfPeaks();
            if (sumOfPeaks >= (2 * getSumOfTheValleys())
                    && sumOfPeaks > getNextSumOfPeaks()
                    && oldSumOfPeaks < sumOfPeaks) {
                if (RawMessage.size(getByte(FIRST_BYTE)) == RawMessage.LENGTH) {
                    bytes[FIRST_BYTE] = getByte(FIRST_BYTE);
                    for (int i = 1; i < MESSAGE_LENGTH; i++) {
                        bytes[i] = getByte(i);
                    }
                    RawMessage rawMessage = RawMessage.of(powerWindow.position() * CONVERSION_TO_NS, bytes);
                    if (rawMessage != null) {
                        powerWindow.advanceBy(WINDOW_SIZE);
                        return rawMessage;
                    }
                }
            }
            oldSumOfPeaks = getSumOfPeaks();
            powerWindow.advance();
        }
        return null;
    }

    private byte getByte(int index) {
        int byteOfMessage = 0;
        for (int j = 0; j < Byte.SIZE; j++) {
            byteOfMessage = byteOfMessage << 1;
            if (getBit(j + index * Byte.SIZE) == 1) {
                byteOfMessage = byteOfMessage | 1;
            }
        }
        return (byte) byteOfMessage;
    }

    private int getBit(int index) {
        return powerWindow.get(PREAMBLE_OF_SAMPLES + index * 10)
                < powerWindow.get(PREAMBLE_OF_SAMPLES + 5 + index * 10) ? 0 : 1;
    }

    private int getSumOfPeaks() {
        return powerWindow.get(0) + powerWindow.get(10) + powerWindow.get(35) + powerWindow.get(45);
    }

    private int getSumOfTheValleys() {
        return powerWindow.get(5) + powerWindow.get(15) + powerWindow.get(20)
                + powerWindow.get(25) + powerWindow.get(30) + powerWindow.get(40);
    }

    private int getNextSumOfPeaks() {
        return powerWindow.get(1) + powerWindow.get(11) + powerWindow.get(36) + powerWindow.get(46);
    }
}
