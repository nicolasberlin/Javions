package ch.epfl.javions.aircraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.ZipFile;

/**
 * Represents the aircraft mictronics database
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class AircraftDatabase {
    private final String fileName;
    private static final String FORMAT = ".csv";
    private static final int LENGHT_ICAO_ADDRES = 6;

    /**
     * Constructs and object that represents the aircraft mictronics database, stored in the filed and checks if the file is not null
     *
     * @param fileName name of file resources
     * @throws NullPointerException if the fileName is null.
     */

    public AircraftDatabase(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    /**
     * Gets information about the aircraft with the ICAO address
     *
     * @param address aircraft's ICAO address
     * @return null if the aircraft does not exist in the file
     * @throws IOException if an error occurs while reading a file
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        String lastTwoLetters = address.string().substring(LENGHT_ICAO_ADDRES - 2);
        try (ZipFile zipFile = new ZipFile(fileName);
             InputStream stream = zipFile.getInputStream(zipFile.getEntry(lastTwoLetters + FORMAT));
             Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String emptyString;

            while ((emptyString = bufferedReader.readLine()) != null) {
                if (emptyString.compareTo(address.string()) >= 0) {
                    break;
                }
            }
            if (emptyString != null && emptyString.startsWith(address.string())) {
                var stringTable = emptyString.split(",", -1);
                return new AircraftData(new AircraftRegistration(stringTable[1]),
                        new AircraftTypeDesignator(stringTable[2]),
                        stringTable[3],
                        new AircraftDescription(stringTable[4]),
                        WakeTurbulenceCategory.of(stringTable[5]));
            }
        }
        return null;
    }
}